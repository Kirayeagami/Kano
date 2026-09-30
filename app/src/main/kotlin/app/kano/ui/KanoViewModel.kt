package app.kano.ui

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import app.kano.AppGraph
import app.kano.data.CareStatus
import app.kano.data.KnowledgeRecord
import app.kano.data.MediaRecord
import app.kano.data.MediaRepository
import app.kano.platform.DeviceSnapshot
import app.kano.platform.MediaIndexWorker
import app.kano.platform.GalleryAccess
import app.kano.platform.GalleryFilter
import app.kano.platform.GalleryPage
import app.kano.platform.MediaIntelligenceResult
import app.kano.platform.VisionCandidate
import app.kano.data.Confidence
import app.kano.core.PrivacyFirewall
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import app.kano.core.VisionPolicy
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DeviceState {
    data object Loading : DeviceState
    data class Ready(val snapshot: DeviceSnapshot) : DeviceState
    data object Failed : DeviceState
}

sealed interface GalleryState {
    data object Loading : GalleryState
    data class Ready(val page: GalleryPage, val access: GalleryAccess) : GalleryState
    data class NoAccess(val access: GalleryAccess) : GalleryState
    data object Failed : GalleryState
}
data class MemoryReport(val beforeAvailable: Long?, val afterAvailable: Long?, val cacheBefore: Long, val cacheAfter: Long, val capturedAt: Long)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class KanoViewModel(private val graph: AppGraph) : ViewModel() {
    val storageError = MutableStateFlow<String?>(null)
    val storageAccess = MutableStateFlow<app.kano.platform.StorageAccessSnapshot?>(null)
    val storageAccessRefreshing = MutableStateFlow(true)
    private var storageAccessJob: Job? = null
    val storageFilter = MutableStateFlow("ALL")
    val storagePage = MutableStateFlow(0)
    val temporaryBytes = MutableStateFlow<Long?>(null)
    val storageScan = graph.database.storage().observeScan()
        .catch { storageError.value = "Storage scan status unavailable."; emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val storageTotals = graph.database.storage().totals()
        .map { it as app.kano.data.StorageTotals? }
        .catch { storageError.value = "Storage totals unavailable."; emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val storageCategories = graph.database.storage().categories()
        .catch { storageError.value = "Storage breakdown unavailable."; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val storageDuplicates = graph.database.storage().duplicates()
        .catch { storageError.value = "Duplicate results unavailable."; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val storageRows = combine(storageFilter, storagePage) { filter, page -> filter to page }
        .flatMapLatest { (filter, page) -> graph.database.storage().page(filter, 100L * 1024 * 1024, 40, page * 40).onStart { emit(emptyList()) } }
        .catch { storageError.value = "Storage file list unavailable."; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun currentStorageAccess() = graph.storageSources.access()
    fun storagePermissions() = graph.storageSources.permissions()
    fun refreshStorageAccess() {
        storageAccessJob?.cancel()
        storageAccessRefreshing.value = true
        storageAccessJob = viewModelScope.launch {
            try {
                val previous = withContext(Dispatchers.IO) { graph.database.storage().scan() }
                val access = graph.storage.refreshAccess()
                if (previous != null && (access.visualSelection || previous.scopeSignature != access.signature)) {
                    // Wait for revoked-cache emissions before publishing the new authorization.
                    // These subscriptions also start Room flows when the screen has just resumed.
                    storageTotals.first { it?.count == 0L }
                    storageCategories.first { it.isEmpty() }
                    storageDuplicates.first { it.isEmpty() }
                    storageRows.first { it.isEmpty() }
                }
                storageAccess.value = access
                temporaryBytes.value = graph.storage.temporaryBytes()
                val scan = withContext(Dispatchers.IO) { graph.database.storage().scan() }
                if (access.hasSources && (scan == null || scan.status == "STALE_INDEX")) graph.storage.requestScan()
                storageError.value = null
                storageAccessRefreshing.value = false
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { storageError.value = "Storage access could not be refreshed. Retry; cached metadata remains hidden." }
        }
    }
    fun scanStorage(force: Boolean = false) = storageAction { graph.storage.requestScan(force) }
    fun pauseStorage() = storageAction { graph.storage.stop(true) }
    fun cancelStorage() = storageAction { graph.storage.stop(false) }
    fun resumeStorage() = storageAction { graph.storage.requestScan(resume = true) }
    fun selectStorage(uris: List<Uri>) = storageAction {
        graph.storage.addGrants(uris)
        storageAccess.value = currentStorageAccess()
    }
    fun setStorageFilter(value: String) { storagePage.value = 0; storageFilter.value = value }
    fun nextStoragePage(direction: Int) { storagePage.value = (storagePage.value + direction).coerceAtLeast(0) }
    suspend fun storageDuplicateMembers(hash: String) = withContext(Dispatchers.IO) { graph.database.storage().duplicateMembers(hash) }
    private fun storageAction(action: suspend () -> Unit) { viewModelScope.launch {
        try { action(); storageError.value = null }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { storageError.value = "Storage operation failed. Retry; originals are retained." }
    } }
    val memoryOptimizing = MutableStateFlow(false)
    val memoryReport = MutableStateFlow<MemoryReport?>(null)
    fun optimizeMemory() {
        if (memoryOptimizing.value) return
        memoryOptimizing.value = true
        memoryReport.value = null
        viewModelScope.launch {
            try {
                val before = graph.device.read()
                val cacheBefore = app.kano.platform.GalleryThumbnails.cachedBytes()
                app.kano.platform.GalleryThumbnails.clear()
                val cacheAfter = app.kano.platform.GalleryThumbnails.cachedBytes()
                val after = graph.device.read()
                memoryReport.value = MemoryReport(before.memoryAvailable, after.memoryAvailable, cacheBefore, cacheAfter, after.capturedAt)
                device.value = DeviceState.Ready(after)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message.value = "Memory measurements unavailable. No improvement is assumed." }
            finally { memoryOptimizing.value = false }
        }
    }
    val gallery = MutableStateFlow<GalleryState>(GalleryState.Loading)
    val galleryFilter = MutableStateFlow(GalleryFilter.ALL)
    val galleryQuery = MutableStateFlow("")
    val galleryOffset = MutableStateFlow(0)
    private var galleryJob: Job? = null
    private var previouslyGranted = false
    val visionResult = MutableStateFlow<MediaIntelligenceResult?>(null)
    val visionAnalyzing = MutableStateFlow(false)
    val visionSaving = MutableStateFlow(false)
    val visionError = MutableStateFlow<String?>(null)
    fun galleryPermissions() = graph.gallery.permissions()
    fun currentGalleryAccess() = graph.gallery.access()
    fun refreshGallery(reset: Boolean = true) {
        galleryJob?.cancel()
        if (reset) galleryOffset.value = 0
        val access = graph.gallery.access()
        if (access == GalleryAccess.DENIED) {
            gallery.value = GalleryState.NoAccess(if (previouslyGranted) GalleryAccess.REVOKED else access)
            return
        }
        previouslyGranted = true
        gallery.value = GalleryState.Loading
        galleryJob = viewModelScope.launch {
            try {
                val page = graph.gallery.page(galleryOffset.value, galleryFilter.value, galleryQuery.value)
                val current = graph.gallery.access()
                if (current != access) { refreshGallery(); return@launch }
                gallery.value = GalleryState.Ready(page, current)
            }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: SecurityException) { gallery.value = GalleryState.NoAccess(GalleryAccess.REVOKED) }
            catch (_: Exception) { gallery.value = GalleryState.Failed }
        }
    }
    fun changeGalleryFilter(value: GalleryFilter) { galleryFilter.value = value; refreshGallery() }
    fun searchGallery(value: String) { galleryQuery.value = value.take(160); refreshGallery() }
    fun nextGalleryPage(direction: Int) { galleryOffset.value = (galleryOffset.value + direction * app.kano.platform.GalleryRepository.PAGE_SIZE).coerceAtLeast(0); refreshGallery(false) }
    val device = MutableStateFlow<DeviceState>(DeviceState.Loading)
    val query = MutableStateFlow("")
    val page = MutableStateFlow(0)
    val message = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    val databaseError = MutableStateFlow(false)
    val indexLoaded = MutableStateFlow(false)
    val rows = combine(query, page) { search, index -> search to index }
        .debounce(200)
        .flatMapLatest { (search, index) ->
            graph.database.media().observe(MediaRepository.searchPattern(search), PAGE_SIZE, index * PAGE_SIZE)
                .catch { databaseError.value = true; emit(emptyList()) }
                .onEach { indexLoaded.value = true }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<MediaRecord>())
    val count = graph.database.media().observeCount()
        .catch { databaseError.value = true; emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val jobs = graph.work.getWorkInfosForUniqueWorkFlow(INDEX_WORK)
        .catch { message.value = "Job status unavailable. Reopen Kano to retry."; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val themeMode = graph.themeManager.themeMode
    val glass = graph.themeManager.glass
    val reducedMotion = graph.themeManager.reducedMotion
    val visualTheme = graph.themeManager.visualTheme
    val glassMode = graph.themeManager.glassMode
    fun setVisualTheme(theme: KanoVisualTheme) = graph.themeManager.setVisualTheme(theme)
    fun setGlassMode(mode: KanoGlassMode) = graph.themeManager.setGlassMode(mode)
    fun setGlass(enabled: Boolean) = graph.themeManager.setGlass(enabled)
    fun setReducedMotion(enabled: Boolean) = graph.themeManager.setReducedMotion(enabled)
    val careItems = graph.care.items.mapCareState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CareState.Loading)
    val careSaving = MutableStateFlow(false)
    val careError = MutableStateFlow<String?>(null)
    val knowledgeError = MutableStateFlow(false)
    val knowledgeLoaded = MutableStateFlow(false)

    // Knowledge Vault & Media OCR States
    val knowledgeEntities = graph.knowledge.allEntities
        .map { records -> records.map { record ->
            if (VisionPolicy.hasSensitiveSignal(record.title + " " + record.detail + " " + record.urlOrPayload.orEmpty()))
                record.copy(title = "Restricted legacy record", detail = "Sensitive legacy content is hidden. You can delete this record from Kano.", urlOrPayload = null, confidence = Confidence.UNCERTAIN.name)
            else record
        } }
        .onEach { knowledgeLoaded.value = true; knowledgeError.value = false }
        .catch { knowledgeError.value = true; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<KnowledgeRecord>())
    val knowledgeCount = graph.knowledge.totalCount
        .catch { knowledgeError.value = true; emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun runMediaOcr(record: MediaRecord) = runLocalVision(record.uri.toUri())
    private var visionJob: Job? = null
    fun cancelLocalVision() { visionJob?.cancel() }

    fun runLocalVision(uri: Uri) {
        if (busy.value) return
        busy.value = true
        visionAnalyzing.value = true
        visionJob = viewModelScope.launch {
            try {
                visionError.value = null
                visionResult.value = null
                visionResult.value = graph.mediaProcessor.processImageUri(uri)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                message.value = "Local OCR scan failed for this document. Verify file read grant."
            } finally {
                busy.value = false
                visionAnalyzing.value = false
            }
        }
    }

    fun saveVisionCandidate(candidate: VisionCandidate) {
        val reviewed = visionResult.value ?: return
        if (visionSaving.value || candidate !in reviewed.candidates || reviewed.isSensitiveRedacted) return
        visionSaving.value = true
        visionError.value = null
        viewModelScope.launch {
            try {
                val latest = graph.mediaProcessor.processImageUri(reviewed.sourceUri.toUri())
                check(latest.digest == reviewed.digest && !latest.isSensitiveRedacted) { "Source changed" }
                graph.knowledge.save(candidate.title, candidate.detail, candidate.type, reviewed.sourceUri,
                    candidate.extraction, Confidence.UNCERTAIN, candidate.url,
                    id = PrivacyFirewall.digest(reviewed.sourceUri + reviewed.digest + candidate.type.name + candidate.detail + candidate.url.orEmpty()))
                message.value = "Reviewed item saved to Vault."
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { visionError.value = "Could not save this item. The source may have changed, access may be revoked, or the Vault may be full. Scan again." }
            finally { visionSaving.value = false }
        }
    }

    fun deleteKnowledgeEntity(id: String) {
        viewModelScope.launch {
            try {
                graph.knowledge.delete(id)
                message.value = "Knowledge entity removed from local Vault."
            } catch (_: Exception) {
                message.value = "Could not delete knowledge entity."
            }
        }
    }

    fun saveCare(id: String?, name: String, category: String, status: CareStatus, done: () -> Unit) = careMutation(done) {
        graph.care.save(id, name, category, status)
    }

    fun deleteCare(id: String, done: () -> Unit) = careMutation(done) { graph.care.delete(id) }

    private fun careMutation(done: () -> Unit, action: suspend () -> Unit) {
        if (careSaving.value) return
        careSaving.value = true
        careError.value = null
        viewModelScope.launch {
            try { action(); done() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { careError.value = "Could not save this change. Your form is kept; retry. Inventory limit: 200 items." }
            finally { careSaving.value = false }
        }
    }
    fun setThemeMode(mode: KanoThemeMode) { graph.themeManager.setThemeMode(mode) }

    init {
        // Sampling is controlled by the Activity foreground lifecycle.
    }

    private var deviceJob: Job? = null
    private var storageObserverJob: Job? = null
    fun setForeground(active: Boolean) {
        deviceJob?.cancel()
        storageObserverJob?.cancel()
        if (!active) { galleryJob?.cancel(); return }
        storageObserverJob = viewModelScope.launch {
            graph.storage.changes().debounce(2_000).collect {
                if (graph.database.storage().scan() != null) refreshStorageAccess()
            }
        }
        deviceJob = viewModelScope.launch {
            graph.device.observeLiveMetrics()
                .catch { device.value = DeviceState.Failed }
                .collect { snapshot ->
                    device.value = DeviceState.Ready(snapshot)
                }
        }
    }

    fun refreshDevice() {
        viewModelScope.launch {
            device.value = DeviceState.Loading
            try { device.value = DeviceState.Ready(graph.device.read()) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { device.value = DeviceState.Failed }
        }
    }

    fun search(value: String) { page.value = 0; query.value = value.take(160) }

    fun select(uris: List<Uri>) {
        if (uris.isEmpty() || busy.value) return
        mutate {
            val summary = graph.media.import(uris)
            message.value = "${summary.accepted} selected; ${summary.rejected} could not be added. Limit: 100 documents."
            page.value = 0
            if (summary.accepted > 0) enqueue()
        }
    }

    fun scan() = mutate { enqueue(); message.value = "Index queued. Waiting for sufficient battery and storage." }

    fun cancel() = mutate {
        withContext(Dispatchers.IO) { graph.work.cancelUniqueWork(INDEX_WORK).result.get() }
        message.value = "Stop requested. Refresh index to resume unfinished items."
    }

    fun forget() = mutate {
        withContext(Dispatchers.IO) { graph.work.cancelUniqueWork(INDEX_WORK).result.get() }
        val unreleased = graph.media.forget()
        page.value = 0
        query.value = ""
        message.value = if (unreleased == 0) "Index forgotten. Original files are unchanged."
            else "Index forgotten. $unreleased access grants need review in Android app settings."
    }

    private suspend fun enqueue() {
        val request = OneTimeWorkRequestBuilder<MediaIndexWorker>()
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).setRequiresStorageNotLow(true).build())
            .build()
        withContext(Dispatchers.IO) {
            graph.work.enqueueUniqueWork(INDEX_WORK, ExistingWorkPolicy.KEEP, request).result.get()
        }
    }

    private fun mutate(action: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message.value = "Operation could not finish. Retry, or reopen Kano if the problem continues." }
            finally { busy.value = false }
        }
    }

    companion object {
        const val PAGE_SIZE = 25
        const val INDEX_WORK = "kano-media-index"
        fun factory(graph: AppGraph): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(KanoViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return KanoViewModel(graph) as T
            }
        }
    }
}

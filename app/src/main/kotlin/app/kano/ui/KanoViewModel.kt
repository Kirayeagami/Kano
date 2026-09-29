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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DeviceState {
    data object Loading : DeviceState
    data class Ready(val snapshot: DeviceSnapshot) : DeviceState
    data object Failed : DeviceState
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class KanoViewModel(private val graph: AppGraph) : ViewModel() {
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
    fun setGlass(enabled: Boolean) = graph.themeManager.setGlass(enabled)
    fun setReducedMotion(enabled: Boolean) = graph.themeManager.setReducedMotion(enabled)
    val careItems = graph.care.items.mapCareState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CareState.Loading)
    val careSaving = MutableStateFlow(false)
    val careError = MutableStateFlow<String?>(null)

    // Knowledge Vault & Media OCR States
    val knowledgeEntities = graph.knowledge.allEntities
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<KnowledgeRecord>())
    val knowledgeCount = graph.knowledge.totalCount
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun runMediaOcr(record: MediaRecord) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                val result = graph.mediaProcessor.processImageUri(record.uri.toUri())
                message.value = when {
                    result.recognizedEntities > 0 -> "Local scan complete: ${result.recognizedEntities} knowledge items extracted into Vault."
                    !result.qrPayload.isNullOrBlank() -> "QR code extracted: ${result.qrPayload}"
                    !result.extractedText.isNullOrBlank() -> "OCR text recognized locally. Saved to Knowledge Vault."
                    else -> "Local OCR scan finished: No readable text or QR codes detected in this document."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                message.value = "Local OCR scan failed for this document. Verify file read grant."
            } finally {
                busy.value = false
            }
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

    init { refreshDevice() }

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

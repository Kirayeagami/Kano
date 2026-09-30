package app.kano.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.data.StorageDuplicateTotal
import app.kano.data.StorageEntry
import app.kano.data.StorageScan
import kotlinx.coroutines.CancellationException
import java.text.DateFormat
import java.util.Date

/** Review-only metadata UI. No cleanup or source deletion is offered in Phase 2. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StorageScreen(model: KanoViewModel, footerInset: Dp = 0.dp) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scan by model.storageScan.collectAsStateWithLifecycle()
    val totals by model.storageTotals.collectAsStateWithLifecycle()
    val categories by model.storageCategories.collectAsStateWithLifecycle()
    val duplicates by model.storageDuplicates.collectAsStateWithLifecycle()
    val rows by model.storageRows.collectAsStateWithLifecycle()
    val filter by model.storageFilter.collectAsStateWithLifecycle()
    val offset by model.storagePage.collectAsStateWithLifecycle()
    val access by model.storageAccess.collectAsStateWithLifecycle()
    val accessRefreshing by model.storageAccessRefreshing.collectAsStateWithLifecycle()
    val error by model.storageError.collectAsStateWithLifecycle()
    val temporary by model.temporaryBytes.collectAsStateWithLifecycle()
    val device by model.device.collectAsStateWithLifecycle()
    val snapshot = (device as? DeviceState.Ready)?.snapshot
    val colors = KanoThemeColors
    var page by rememberSaveable { mutableStateOf("Overview") }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var filterInitialized by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<StorageEntry?>(null) }
    var duplicate by remember { mutableStateOf<StorageDuplicateTotal?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val listState = remember(page, filter) { LazyListState() }
    val grants = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { model.refreshStorageAccess() }
    val documents = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { model.selectStorage(it) }
    val folder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { model.selectStorage(listOf(it)) } }
    val active = scan?.status in setOf("QUEUED", "SCANNING", "UPDATING")
    // Cached metadata from a different authorization must not remain visible after revocation.
    val authorizedIndex = !accessRefreshing && access != null && scan != null && scan?.scopeSignature == access?.signature
    val expectedFilter = when (page) { "Large files" -> "LARGE"; "Downloads" -> "DOWNLOADS"; "Files" -> selectedCategory; else -> "ALL" }
    val filteredRows = rows.filter { entry ->
        when (expectedFilter) {
            "ALL" -> true
            "LARGE" -> (entry.sizeBytes ?: -1) >= 100L * 1024 * 1024
            "DOWNLOADS" -> entry.scope.startsWith("saf:") || entry.location?.contains("Download", ignoreCase = true) == true
            else -> entry.category == expectedFilter
        }
    }
    val rowsReady = filterInitialized && filter == expectedFilter
    val canShowCachedDetails = authorizedIndex && scan?.status !in setOf("STALE_INDEX", "QUEUED", "SCANNING")

    fun bytes(value: Long?) = value?.let { Formatter.formatFileSize(context, it) } ?: "Unavailable"
    fun navigate(target: String, category: String? = null) {
        selectedCategory = category ?: "ALL"
        page = target
        model.setStorageFilter(category ?: when (target) { "Large files" -> "LARGE"; "Downloads" -> "DOWNLOADS"; else -> "ALL" })
    }
    fun appSettings() {
        try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
        catch (_: Exception) { actionError = "Android app settings are unavailable." }
    }
    fun open(entry: StorageEntry) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(entry.uri), entry.mime ?: "*/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (_: Exception) { actionError = "No app can open this file, or its read access is no longer available." }
    }
    BackHandler(page != "Overview") { navigate("Overview") }
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { selected = null; duplicate = null; model.refreshStorageAccess() }
            if (event == Lifecycle.Event.ON_STOP) { selected = null; duplicate = null }
        }
        owner.lifecycle.addObserver(observer)
        model.refreshStorageAccess()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(access?.signature, accessRefreshing, scan?.status) {
        if (accessRefreshing || !authorizedIndex || scan?.status in setOf("STALE_INDEX", "QUEUED", "SCANNING")) {
            selected = null
            duplicate = null
        }
    }
    LaunchedEffect(page, selectedCategory) {
        filterInitialized = false
        if (model.storageFilter.value != expectedFilter) model.setStorageFilter(expectedFilter)
        filterInitialized = true
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.testTag("storage-screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp + footerInset),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Storage intelligence", "Local metadata · review only")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Overview", "Breakdown", "Duplicates", "Large files", "Downloads", "Temporary", "Files").forEach { target ->
                    FilterChip(page == target, { navigate(target) }, label = { Text(target) }, modifier = Modifier.heightIn(min = 48.dp))
                }
            }
        }
        if (page == "Overview") {
            item {
                KanoGlassCard {
                    Text("Device data volume", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    val total = snapshot?.storageTotal?.takeIf { it > 0 }
                    val available = snapshot?.storageAvailable?.takeIf { it >= 0 && total != null && it <= total }
                    FactRow("Total", bytes(total))
                    FactRow("Used", bytes(if (total != null && available != null) total - available else null))
                    FactRow("Available", bytes(available))
                    Text("StatFs on Kano's data volume. Includes space Kano cannot inspect; this is not the indexed-file total.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    if (total != null && available != null && available * 1.0 / total <= .1) {
                        Text("Less than 10% available. Review large files and duplicates before taking any action.", style = MaterialTheme.typography.bodyMedium, color = colors.warning)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Authorized index measurements", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    FactRow("Accessible MediaStore", if (authorizedIndex) bytes(totals?.mediaBytes) else "Unavailable", "Reported sizes of indexed media sources only. Does not include ungranted media or private app storage.")
                    FactRow("Selected documents", if (authorizedIndex) bytes(totals?.fileBytes) else "Unavailable", "Reported sizes of read-granted SAF files. Cloud-backed or provider-logical sizes are not physical device usage.")
                    FactRow("Kano private data", bytes(snapshot?.kanoStorageBytes), "Phase 1 own-data reading: files, database and caches. Not an amount safe to remove.")
                    Text("These measurements have different scopes and must not be added to the StatFs used-storage figure.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                }
            }
        }
        item {
            Text(if (accessRefreshing) "Rechecking authorized access…" else access?.label ?: "Checking authorized access…", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            if (access?.limited == true) Text("Limited scope. Categories and duplicates cover authorized items only.", style = MaterialTheme.typography.bodySmall, color = colors.warning)
            if (!authorizedIndex && scan != null) Text("Access changed or the index is stale. Update the scan before reviewing cached file metadata.", style = MaterialTheme.typography.bodySmall, color = colors.warning)
            if (page != "Overview" && scan?.status != "COMPLETED") Text("Index state: ${storageLabel(scan?.status ?: "FIRST_RUN")}. Results may be incomplete; return to Overview for scan controls.", style = MaterialTheme.typography.bodySmall, color = colors.warning)
        }
        if (page == "Overview") {
            item {
                StorageScanStatus(scan, active)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (scan?.status) {
                        "SCANNING", "UPDATING" -> {
                            KanoOutlinedButton(model::pauseStorage) { Text("Pause scan") }
                            KanoOutlinedButton(model::cancelStorage) { Text("Cancel scan") }
                        }
                        "QUEUED" -> KanoOutlinedButton(model::cancelStorage) { Text("Cancel scan") }
                        "PAUSED", "INTERRUPTED", "CANCELLED", "ERROR", "PARTIAL_FAILURE" -> {
                            KanoOutlinedButton(model::resumeStorage, enabled = access?.hasSources == true) { Text("Resume scan") }
                            if (scan?.status == "PAUSED") KanoOutlinedButton(model::cancelStorage) { Text("Cancel scan") }
                            KanoOutlinedButton({ model.scanStorage(true) }, enabled = access?.hasSources == true) { Text("Rescan all authorized items") }
                        }
                        else -> {
                            KanoHeroButton({ model.scanStorage() }, enabled = access?.hasSources == true) { Text(if (scan == null || scan?.status == "FIRST_RUN") "Scan authorized storage" else "Update storage index") }
                            if (scan != null) KanoOutlinedButton({ model.scanStorage(true) }, enabled = access?.hasSources == true) { Text("Rescan all authorized items") }
                        }
                    }
                }
            }
            item {
                if (authorizedIndex) {
                    KanoCardGroup {
                        val summary = totals?.let { "${it.count} indexed · ${if (it.count == 0L) "no files" else bytes(it.bytes)} reported · ${it.unknownSizes} sizes unknown" } ?: "Reading index totals…"
                        KanoGroupItem("Storage breakdown", summary, { navigate("Breakdown") })
                        KanoGroupItem("Exact duplicate review", "Accessible-byte matches · potential space only", { navigate("Duplicates") })
                        KanoGroupItem("Large files", "100 MiB and above · large does not mean unnecessary", { navigate("Large files") })
                        KanoGroupItem("Downloads & selected files", "Read-granted documents and visible download locations", { navigate("Downloads") })
                        KanoGroupItem("Kano generated code cache", bytes(temporary) + " · measured only", { navigate("Temporary") }, showDivider = false)
                    }
                } else {
                    KanoStateSurface(if (access?.hasSources == false) "No authorized storage source" else "Index not ready", "Grant access or run a scan. Unknown content is kept; no files are removed.")
                }
            }
            if (authorizedIndex && scan?.status == "COMPLETED" && duplicates.isNotEmpty()) item {
                val candidates = duplicates.sumOf { it.copies - 1 }
                val amounts = duplicates.map { if (it.localIdentityVerified) storageProduct(it.sizeBytes, it.copies - 1) else null }
                val recoverable = storageSum(amounts)
                KanoGlassCard {
                    Text("Duplicate review recommendation", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Text("$candidates potential extra indexed entries in ${duplicates.size} matching groups shown · ${bytes(recoverable)} potentially recoverable.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Text("Evidence: matching SHA-256 and readable byte counts. Source: authorized index · scan finished ${storageDate(scan?.finishedAt)}. Confidence: exact accessible-byte matches. Risk: retain the copies you need; redaction affects original equality. Physical copies and local recovery are unconfirmed for arbitrary SAF aliases or cloud-backed entries. No deletion is available.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                    KanoOutlinedButton({ navigate("Duplicates") }) { Text("Review duplicate evidence") }
                }
            }
            if (authorizedIndex && scan?.status == "COMPLETED") {
                val large = rows.filter { (it.sizeBytes ?: -1) >= 100L * 1024 * 1024 }
                if (large.isNotEmpty()) item {
                    StorageReviewEvidence(
                        "Large-file review recommendation",
                        "${large.size} items on the current 40-item page report at least 100 MiB · ${bytes(storageSum(large.map { it.sizeBytes }))} reported combined size.",
                        "Evidence: provider size metadata meets the review threshold. Source: authorized index · latest page item indexed ${storageDate(large.maxOfOrNull { it.indexedAt })}. Confidence: reported metadata, not a physical-space recovery guarantee. Risk: large files may be important; size alone is never a delete recommendation.",
                        "Review large-file metadata"
                    ) { navigate("Large files") }
                }
                val downloads = rows.filter { (it.sizeBytes ?: 0) > 0 && (it.scope.startsWith("saf:") || it.location?.contains("Download", ignoreCase = true) == true) }
                if (downloads.isNotEmpty()) item {
                    StorageReviewEvidence(
                        "Download and selection review",
                        "${downloads.size} visible download-location or SAF-selected entries on the current 40-item page · ${bytes(storageSum(downloads.map { it.sizeBytes }))} reported combined size.",
                        "Evidence: reported location or explicit file selection. Source: authorized index · latest page item indexed ${storageDate(downloads.maxOfOrNull { it.indexedAt })}. Confidence: metadata association only; SAF selection does not prove a download. Risk: documents may be useful or cloud-backed. Age and installer/archive type alone do not mean unnecessary.",
                        "Review downloads and selected files"
                    ) { navigate("Downloads") }
                }
            }
            if (temporary != null && temporary!! > 0) item {
                StorageReviewEvidence(
                    "Generated code-cache review",
                    "${bytes(temporary)} measured in Kano's generated code cache.",
                    "Evidence: filesystem sizes within Context.codeCacheDir. Source: Kano-private generated files; measurement time is not recorded. Confidence: directory measurement only. Risk: this excludes captures, drafts, normal cache and database; no storage recovery has happened and Phase 2 does not remove it.",
                    "Review generated code cache"
                ) { navigate("Temporary") }
            }
            item {
                Text("Access controls", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    KanoOutlinedButton({ grants.launch(model.storagePermissions()) }) { Text("Manage media access") }
                    KanoOutlinedButton({ documents.launch(arrayOf("*/*")) }) { Text("Choose files") }
                    KanoOutlinedButton({ folder.launch(null) }) { Text("Choose a folder") }
                    KanoOutlinedButton(::appSettings) { Text("Android app settings") }
                }
                Text("Android controls which folders are selectable. Root storage, Download root and another app's private data are not promised. A selected document may be cloud-backed; its logical size is not a measurement of local physical space.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
            }
        } else if (page == "Temporary") {
            item {
                KanoGlassCard {
                    Text("Kano generated code cache", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    FactRow("Measured generated code cache", bytes(temporary), "Kano's codeCacheDir only; generated files Android can reconstruct.")
                    FactRow("Kano private data", bytes(snapshot?.kanoStorageBytes), "Phase 1 own-data measurement; database, files and caches. Not an amount safe to remove.")
                    Text("Pending camera captures, normal cache files, drafts and database records are excluded from this temporary-data estimate. Other Android-managed data and other apps' private caches are unavailable to this scanner. Phase 2 offers no removal.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    KanoOutlinedButton({ model.refreshStorageAccess() }) { Text("Refresh code-cache measurement") }
                }
            }
        } else if (!authorizedIndex) {
            item { KanoStateSurface("Storage review unavailable", "Return to Overview to grant access and update the index. Previously cached metadata is hidden when authorization changes.") }
        } else when (page) {
            "Breakdown" -> {
                item { Text("Primary categories of indexed items. Covers authorized content only; not the whole device. Downloads, large files and duplicate review overlap these categories.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
                if (categories.isEmpty()) item { KanoStateSurface("No categories indexed", "The authorized index is empty, or the scan has not read any items yet.") }
                else item {
                    KanoCardGroup {
                        categories.forEachIndexed { index, category ->
                            KanoGroupItem(storageLabel(category.category), "${category.count} items · ${bytes(category.bytes)} reported" + if (category.unknownSizes > 0) " · ${category.unknownSizes} sizes unknown" else "", { navigate("Files", category.category) }, showDivider = index != categories.lastIndex)
                        }
                    }
                }
                item { Text("Inaccessible app data and ungranted files: not fully measurable. Missing categories do not mean the phone contains zero items in them.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary) }
            }
            "Duplicates" -> {
                item { Text("Last indexed SHA-256 and byte counts match for accessible reads. Android may redact media metadata; equality of original unredacted files is not fully verified. Potential recovery is an estimate, never actually recovered space.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
                if (duplicates.isEmpty()) item { KanoStateSurface("No verified duplicate groups", "Candidate hashes may still be pending, unreadable or outside the authorized scope. No files have been removed.") }
                items(duplicates, key = { it.sha256 + ":" + it.sizeBytes }) { group ->
                    KanoActionRow("${group.copies} matching indexed entries", "${bytes(group.sizeBytes)} each · ${bytes(storageProduct(group.sizeBytes, group.copies))} reported total · ${bytes(if (group.localIdentityVerified) storageProduct(group.sizeBytes, group.copies - 1) else null)} potentially recoverable" + if (!group.localIdentityVerified) " · physical copies unconfirmed" else "", {
                        if (canShowCachedDetails) duplicate = group else actionError = "Pause or finish the scan before opening duplicate details."
                    })
                }
                item { Text("Up to 40 matching groups, ordered by potential size. Review what to keep; deletion is a later phase.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary) }
            }
            else -> {
                item {
                    val title = when (page) { "Large files" -> "Large file review"; "Downloads" -> "Downloads & selected files"; else -> if (expectedFilter == "ALL") "Authorized indexed files" else storageLabel(expectedFilter) }
                    Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    Text(when (page) { "Large files" -> "Shown because size is at least 100 MiB. 500 MiB and 1 GiB markers describe size, not usefulness."; "Downloads" -> "Visible download locations and SAF-selected files. Selection alone does not prove a file is a download."; else -> "Metadata only; unknown classification means keep. File contents are not sent to AI." }, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                if (!rowsReady) item { KanoStateSurface("Updating file view", "Restoring the requested category.", true) }
                else if (filteredRows.isEmpty()) item { KanoStateSurface("No indexed matches on this page", "Change the category or update the authorized index. Nothing is assumed unnecessary.") }
                items(if (rowsReady) filteredRows else emptyList(), key = { it.identity }) { entry ->
                    KanoActionRow(entry.name, bytes(entry.sizeBytes) + " · " + storageLabel(entry.category) + (entry.sizeBytes?.let { if (it >= 1024L * 1024 * 1024) " · 1 GiB+" else if (it >= 500L * 1024 * 1024) " · 500 MiB+" else "" } ?: ""), {
                        if (canShowCachedDetails) selected = entry else actionError = "Pause or finish the scan before opening file details."
                    })
                }
                item {
                    Text("Page ${offset + 1} · up to 40 items", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KanoOutlinedButton({ model.nextStoragePage(-1) }, enabled = rowsReady && offset > 0) { Text("Previous files") }
                        KanoOutlinedButton({ model.nextStoragePage(1) }, enabled = rowsReady && filteredRows.size == 40) { Text("Next files") }
                    }
                }
            }
        }
        error?.let { detail -> item { Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.error) } }
        actionError?.let { detail -> item { Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.error) } }
        item { Text("Local-first · no deletion · no system-wide cache access", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary) }
    }
    selected?.takeIf { canShowCachedDetails }?.let { entry ->
        ModalBottomSheet(onDismissRequest = { selected = null }, containerColor = colors.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StorageFileFacts(entry)
                KanoHeroButton({ open(entry) }) { Text("Open file") }
                TextButton({ selected = null }) { Text("Close file details") }
                actionError?.let { Text(it, color = colors.error) }
                Spacer(Modifier.navigationBarsPadding().height(16.dp))
            }
        }
    }
    duplicate?.takeIf { canShowCachedDetails }?.let { group ->
        var members by remember(group.sha256) { mutableStateOf<List<StorageEntry>?>(null) }
        var memberError by remember(group.sha256) { mutableStateOf(false) }
        LaunchedEffect(group.sha256, access?.signature) {
            try { members = model.storageDuplicateMembers(group.sha256) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { memberError = true }
        }
        ModalBottomSheet(onDismissRequest = { duplicate = null }, containerColor = colors.surface) {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    SectionTitle("Matching accessible bytes", "${group.copies} indexed entries · ${bytes(if (group.localIdentityVerified) storageProduct(group.sizeBytes, group.copies - 1) else null)} potentially recoverable")
                    Text("Confidence: exact digest and readable byte count. Risk: originals may have redacted metadata; physical copies and physical recovery are unconfirmed for arbitrary SAF identities or cloud providers. Keep decisions require your review. No deletion is available.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                if (members == null && !memberError) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (memberError) item { Text("Duplicate members are unavailable. Close and retry after updating the scan.", color = colors.error) }
                members?.let { entries -> items(entries, key = { it.identity }) { entry -> StorageFileFacts(entry) } }
                item {
                    Text("At most 100 members displayed.", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                    TextButton({ duplicate = null }) { Text("Close duplicate review") }
                    Spacer(Modifier.navigationBarsPadding().height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StorageReviewEvidence(title: String, what: String, evidenceAndRisk: String, action: String, review: () -> Unit) {
    val colors = KanoThemeColors
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Text(what, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(evidenceAndRisk, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
        KanoOutlinedButton(review) { Text(action) }
        HorizontalDivider(color = colors.divider)
    }
}

@Composable
private fun StorageScanStatus(scan: StorageScan?, active: Boolean) {
    val colors = KanoThemeColors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Scan status", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        StatusChip(storageLabel(scan?.status ?: "FIRST_RUN"), tone = when (scan?.status) { "ERROR", "PARTIAL_FAILURE" -> StatusTone.WARNING; "COMPLETED" -> StatusTone.SUCCESS; else -> StatusTone.NEUTRAL })
        Text(scan?.detail ?: "No storage scan has run.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        if (active) {
            if (scan?.phase == "HASHING" && scan.hashCandidates > 0) {
                LinearProgressIndicator(progress = { (scan.hashed.toFloat() / scan.hashCandidates).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${scan.hashed} of ${scan.hashCandidates} candidate reads checked", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            } else LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        scan?.let {
            Text("${it.scanned} read · ${it.added} added · ${it.updated} updated · ${it.removed} no longer indexed · ${it.errors} errors", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text("Started: ${storageDate(it.startedAt)} · Finished: ${storageDate(it.finishedAt)}", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            if (it.startedAt != null && it.finishedAt != null) Text("Duration: ${((it.finishedAt - it.startedAt).coerceAtLeast(0) / 1000)} s", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
        }
    }
}

@Composable
private fun StorageFileFacts(entry: StorageEntry) {
    val context = LocalContext.current
    val colors = KanoThemeColors
    Text(entry.name, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
    FactRow("Size / type", (entry.sizeBytes?.let { Formatter.formatFileSize(context, it) } ?: "Size unavailable") + " · " + (entry.mime ?: "Type unavailable"))
    FactRow("Location", entry.location ?: "Provider did not report a location", "Read source: " + entry.scope)
    FactRow("Added / modified", storageDate(entry.addedAt) + " / " + storageDate(entry.modifiedAt))
    FactRow("Category / classification", storageLabel(entry.category) + " / " + storageLabel(entry.classification), "Unknown means keep. Age, name and size alone never mean unnecessary.")
    FactRow("Access / scan", storageLabel(entry.accessState) + " / " + storageLabel(entry.scanState), "Indexed: " + storageDate(entry.indexedAt))
    FactRow("Hash / sensitivity", storageLabel(entry.hashState) + " / " + storageLabel(entry.sensitivity), "Sensitivity is not inferred safe from metadata.")
    if (entry.width != null && entry.height != null) FactRow("Dimensions", "${entry.width} × ${entry.height}")
    entry.durationMillis?.let { FactRow("Duration", "${it / 1000} s") }
}

private fun storageDate(value: Long?): String = value?.takeIf { it > 0 }?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) } ?: "Unavailable"
private fun storageLabel(value: String): String = when (value) {
    "APKS" -> "APKs"
    "KANO_DATA" -> "Kano data"
    else -> value.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
}
private fun storageProduct(size: Long, copies: Long): Long? = if (size >= 0 && copies >= 0 && (copies == 0L || size <= Long.MAX_VALUE / copies)) size * copies else null
private fun storageSum(values: List<Long?>): Long? {
    var total = 0L
    values.forEach { value ->
        if (value == null || value < 0 || total > Long.MAX_VALUE - value) return null
        total += value
    }
    return total
}

package app.kano.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.data.StorageCategoryTotal
import app.kano.data.StorageDuplicateTotal
import app.kano.data.StorageEntry
import app.kano.data.StorageScan
import app.kano.platform.GalleryThumbnails
import kotlinx.coroutines.CancellationException
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Production-quality Storage Intelligence UX. Local metadata · review-only mode. */
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
    val sort by model.storageSort.collectAsStateWithLifecycle()
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
    var reviewConfirmation by remember { mutableStateOf<Pair<Int, Long>?>(null) }

    val listState = remember(page, filter, sort) { LazyListState() }
    val grants = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { model.refreshStorageAccess() }
    val documents = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { model.selectStorage(it) }
    val folder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { model.selectStorage(listOf(it)) } }
    val active = scan?.status in setOf("QUEUED", "SCANNING", "UPDATING")
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Text("Storage volume capacity reported by system StatFs.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    if (total != null && available != null && available * 1.0 / total <= .1) {
                        Text("Low storage: less than 10% available. Review large files and duplicates to reclaim space.", style = MaterialTheme.typography.bodyMedium, color = colors.warning)
                    }
                }
            }
            item {
                KanoGlassCard {
                    Text("Authorized index measurements", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    FactRow("Accessible media", if (authorizedIndex) bytes(totals?.mediaBytes) else "Unavailable", "Photos, videos, and audio in MediaStore")
                    FactRow("Selected documents", if (authorizedIndex) bytes(totals?.fileBytes) else "Unavailable", "Files and folders granted via Storage Access Framework")
                    FactRow("Kano private data", bytes(snapshot?.kanoStorageBytes), "App database, files, and temporary caches")
                }
            }
        }

        item {
            Text(if (accessRefreshing) "Rechecking storage access…" else access?.label ?: "Checking storage access…", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            if (access?.limited == true) Text("Limited scope: showing authorized items only.", style = MaterialTheme.typography.bodySmall, color = colors.warning)
            if (!authorizedIndex && scan != null) Text("Storage access changed. Run a scan to refresh file index.", style = MaterialTheme.typography.bodySmall, color = colors.warning)
        }

        if (page == "Overview") {
            item {
                StorageScanStatusCard(scan, totals, active)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        val summary = totals?.let { "${it.count} items · ${if (it.count == 0L) "no files" else bytes(it.bytes)}" } ?: "Reading index…"
                        KanoGroupItem("Storage breakdown", summary, { navigate("Breakdown") }, icon = Icons.Outlined.PieChart)
                        KanoGroupItem("Exact duplicate review", "${duplicates.size} groups · ${bytes(storageSum(duplicates.map { if (it.localIdentityVerified) storageProduct(it.sizeBytes, it.copies - 1) else null }))} potential", { navigate("Duplicates") }, icon = Icons.Outlined.ContentCopy)
                        KanoGroupItem("Large files", "100 MB and larger · review size", { navigate("Large files") }, icon = Icons.Outlined.FolderSpecial)
                        KanoGroupItem("Downloads & selected files", "Visible downloads and SAF folders", { navigate("Downloads") }, icon = Icons.Outlined.Download)
                        KanoGroupItem("Kano generated code cache", bytes(temporary) + " · reconstructible", { navigate("Temporary") }, icon = Icons.Outlined.Cached, showDivider = false)
                    }
                } else {
                    KanoStateSurface(if (access?.hasSources == false) "No storage source authorized" else "Index pending", "Grant access or tap scan to index media and documents.")
                }
            }

            // Real-data recommendations
            if (authorizedIndex && scan?.status == "COMPLETED") {
                if (duplicates.isNotEmpty()) item {
                    val candidates = duplicates.sumOf { it.copies - 1 }
                    val amounts = duplicates.map { if (it.localIdentityVerified) storageProduct(it.sizeBytes, it.copies - 1) else null }
                    val recoverable = storageSum(amounts)
                    StorageRecommendationCard(
                        title = "Duplicate files found",
                        subtitle = "$candidates duplicate copies in ${duplicates.size} groups · ${bytes(recoverable)} potentially recoverable",
                        actionLabel = "Review duplicates",
                        icon = Icons.Outlined.ContentCopy,
                        onClick = { navigate("Duplicates") }
                    )
                }

                val large = rows.filter { (it.sizeBytes ?: -1) >= 100L * 1024 * 1024 }
                if (large.isNotEmpty()) item {
                    StorageRecommendationCard(
                        title = "Large files detected",
                        subtitle = "${large.size} files over 100 MB · ${bytes(storageSum(large.map { it.sizeBytes }))} total size",
                        actionLabel = "Review large files",
                        icon = Icons.Outlined.FolderSpecial,
                        onClick = { navigate("Large files") }
                    )
                }

                val downloads = rows.filter { (it.sizeBytes ?: 0) > 0 && (it.scope.startsWith("saf:") || it.location?.contains("Download", ignoreCase = true) == true) }
                if (downloads.isNotEmpty()) item {
                    StorageRecommendationCard(
                        title = "Downloads & selected files",
                        subtitle = "${downloads.size} files · ${bytes(storageSum(downloads.map { it.sizeBytes }))} reported size",
                        actionLabel = "Review downloads",
                        icon = Icons.Outlined.Download,
                        onClick = { navigate("Downloads") }
                    )
                }
            }

            if (temporary != null && temporary!! > 0) item {
                StorageRecommendationCard(
                    title = "Reconstructible code cache",
                    subtitle = "${bytes(temporary)} in Kano generated code cache",
                    actionLabel = "Review code cache",
                    icon = Icons.Outlined.Cached,
                    onClick = { navigate("Temporary") }
                )
            }

            item {
                Text("Access controls", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KanoOutlinedButton({ grants.launch(model.storagePermissions()) }) { Text("Manage media access") }
                    KanoOutlinedButton({ documents.launch(arrayOf("*/*")) }) { Text("Choose files") }
                    KanoOutlinedButton({ folder.launch(null) }) { Text("Choose a folder") }
                    KanoOutlinedButton(::appSettings) { Text("Android app settings") }
                }
            }
        } else if (page == "Temporary") {
            item {
                KanoGlassCard {
                    Text("Kano generated code cache", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    FactRow("Measured code cache", bytes(temporary), "Kano's codeCacheDir only; safe for Android to reconstruct")
                    FactRow("Kano private storage", bytes(snapshot?.kanoStorageBytes), "App database, files, and caches")
                    Text("Pending camera captures, active database records, and drafts are excluded. In Phase 2, this is review-only.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    KanoOutlinedButton({ model.refreshStorageAccess() }) { Text("Refresh measurement") }
                }
            }
        } else if (!authorizedIndex) {
            item { KanoStateSurface("Storage review unavailable", "Return to Overview to grant access and run a scan.") }
        } else when (page) {
            "Breakdown" -> {
                item {
                    Text("Categories of indexed storage", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Text("Tap any category to inspect its indexed files.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                if (categories.isEmpty()) item { KanoStateSurface("No categories indexed", "Run a scan to index your media and documents.") }
                else item {
                    KanoCardGroup {
                        categories.forEachIndexed { index, cat ->
                            KanoGroupItem(
                                title = storageLabel(cat.category),
                                detail = "${cat.count} files · ${bytes(cat.bytes)}",
                                onClick = { navigate("Files", cat.category) },
                                icon = categoryIcon(cat.category),
                                showDivider = index != categories.lastIndex
                            )
                        }
                    }
                }
            }
            "Duplicates" -> {
                val candidates = duplicates.sumOf { it.copies - 1 }
                val recoverable = storageSum(duplicates.map { if (it.localIdentityVerified) storageProduct(it.sizeBytes, it.copies - 1) else null })
                item {
                    KanoGlassCard {
                        Text("Exact duplicate review", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text("${duplicates.size} duplicate groups · $candidates duplicate copies · ${bytes(recoverable)} potentially recoverable", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        Text("Verified by matching SHA-256 hash and byte length. Review individual copies to choose which to retain.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                    }
                }
                if (duplicates.isEmpty()) item {
                    KanoStateSurface("No duplicate files found", "All scanned files have distinct content. Your storage is clean.")
                }
                items(duplicates, key = { it.sha256 + ":" + it.sizeBytes }) { group ->
                    DuplicateGroupCard(
                        group = group,
                        formatBytes = ::bytes,
                        onReview = {
                            if (canShowCachedDetails) duplicate = group else actionError = "Pause or finish the scan before opening duplicate details."
                        }
                    )
                }
            }
            else -> {
                item {
                    val title = when (page) {
                        "Large files" -> "Large file review"
                        "Downloads" -> "Downloads & selected files"
                        else -> if (expectedFilter == "ALL") "All indexed files" else storageLabel(expectedFilter)
                    }
                    val subtitle = when (page) {
                        "Large files" -> "Files 100 MB and larger · inspect media and documents"
                        "Downloads" -> "Files in visible download folders and SAF selections"
                        else -> "Local file metadata · tap to view details or open"
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }

                // Category filter chips row when in "Files"
                if (page == "Files") {
                    item {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "ALL" to "All", "PHOTOS" to "Photos", "VIDEOS" to "Videos",
                                "SCREENSHOTS" to "Screenshots", "SCREEN_RECORDINGS" to "Recordings",
                                "AUDIO" to "Audio", "DOCUMENTS" to "Documents", "APKS" to "APKs",
                                "ARCHIVES" to "Archives", "OTHER" to "Other"
                            ).forEach { (catKey, catLabel) ->
                                FilterChip(
                                    selected = selectedCategory == catKey,
                                    onClick = { navigate("Files", catKey) },
                                    label = { Text(catLabel) },
                                    modifier = Modifier.heightIn(min = 40.dp)
                                )
                            }
                        }
                    }
                }

                // Sort chips bar
                item {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort", tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                        listOf(
                            "SIZE_DESC" to "Largest",
                            "SIZE_ASC" to "Smallest",
                            "DATE_DESC" to "Newest",
                            "DATE_ASC" to "Oldest",
                            "NAME_ASC" to "Name"
                        ).forEach { (sortKey, sortLabel) ->
                            FilterChip(
                                selected = sort == sortKey,
                                onClick = { model.setStorageSort(sortKey) },
                                label = { Text(sortLabel) },
                                modifier = Modifier.heightIn(min = 36.dp)
                            )
                        }
                    }
                }

                if (!rowsReady) item { KanoStateSurface("Updating view…", "Loading files for this category.", true) }
                else if (filteredRows.isEmpty()) item {
                    val emptyMessage = when (page) {
                        "Large files" -> "No large files found · All indexed files are under 100 MB."
                        "Downloads" -> "No downloaded files found in authorized locations."
                        else -> "No files found for ${storageLabel(expectedFilter)} · Your storage is organized."
                    }
                    KanoStateSurface("No matches found", emptyMessage)
                }

                items(if (rowsReady) filteredRows else emptyList(), key = { it.identity }) { entry ->
                    StorageEntryCard(
                        entry = entry,
                        formatBytes = ::bytes,
                        onOpen = { open(entry) },
                        onDetails = {
                            if (canShowCachedDetails) selected = entry else actionError = "Pause or finish the scan before opening file details."
                        }
                    )
                }

                item {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Page ${offset + 1} (${filteredRows.size} items)", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KanoOutlinedButton({ model.nextStoragePage(-1) }, enabled = rowsReady && offset > 0) { Text("Previous") }
                            KanoOutlinedButton({ model.nextStoragePage(1) }, enabled = rowsReady && filteredRows.size == 40) { Text("Next") }
                        }
                    }
                }
            }
        }

        error?.let { detail -> item { Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.error) } }
        actionError?.let { detail -> item { Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.error) } }
        item { Text("Local storage · private review · no background deletion", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary) }
    }

    // Item Details Bottom Sheet
    selected?.takeIf { canShowCachedDetails }?.let { entry ->
        ModalBottomSheet(onDismissRequest = { selected = null }, containerColor = colors.surface) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with preview
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StorageMediaThumbnail(
                        entry = entry,
                        modifier = Modifier.size(92.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(entry.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(bytes(entry.sizeBytes), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.accent)
                        Text(storageLabel(entry.category), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                    }
                }

                HorizontalDivider(color = colors.divider)

                // Clean fact rows
                FactRow("Exact size", entry.sizeBytes?.let { "${bytes(it)} (${String.format(Locale.ROOT, "%,d", it)} bytes)" } ?: "Unavailable")
                FactRow("Format / Type", "${formatExtension(entry.name, entry.mime)} · ${entry.mime ?: "Unknown"}")
                FactRow("Location", formatCleanLocation(entry.location), entry.location ?: "")
                if (entry.width != null && entry.height != null) {
                    val resTag = formatResolutionTag(entry.width, entry.height)
                    FactRow("Dimensions", "${entry.width} × ${entry.height}" + if (resTag != null) " ($resTag)" else "")
                }
                entry.durationMillis?.let { FactRow("Duration", formatDuration(it)) }
                FactRow("Modified", storageDate(entry.modifiedAt ?: entry.addedAt))

                Spacer(Modifier.height(8.dp))

                KanoHeroButton({ open(entry) }) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open file")
                }
                KanoOutlinedButton({ selected = null }) { Text("Close details") }
                actionError?.let { Text(it, color = colors.error) }
                Spacer(Modifier.navigationBarsPadding().height(16.dp))
            }
        }
    }

    // Duplicates Review Bottom Sheet
    duplicate?.takeIf { canShowCachedDetails }?.let { group ->
        var members by remember(group.sha256) { mutableStateOf<List<StorageEntry>?>(null) }
        var memberError by remember(group.sha256) { mutableStateOf(false) }
        val selectedCandidateIds = remember(group.sha256) { mutableStateListOf<String>() }

        LaunchedEffect(group.sha256, access?.signature) {
            try {
                val list = model.storageDuplicateMembers(group.sha256)
                members = list
                // Pre-select all copies except the earliest (original)
                if (list.size > 1) {
                    val earliest = list.minByOrNull { it.addedAt ?: it.modifiedAt ?: Long.MAX_VALUE }
                    list.filter { it.identity != earliest?.identity }.forEach {
                        selectedCandidateIds.add(it.identity)
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { memberError = true }
        }

        ModalBottomSheet(onDismissRequest = { duplicate = null }, containerColor = colors.surface) {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    SectionTitle(
                        "Identical file copies",
                        "${group.copies} copies · ${bytes(group.sizeBytes)} each · ${bytes(if (group.localIdentityVerified) storageProduct(group.sizeBytes, group.copies - 1) else null)} recoverable"
                    )
                    Text("Verified identical byte-for-byte SHA-256 match. The earliest copy is marked as original.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }

                if (members == null && !memberError) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (memberError) item { Text("Duplicate details are unavailable. Retry after updating scan.", color = colors.error) }

                members?.let { entries ->
                    val earliest = entries.minByOrNull { it.addedAt ?: it.modifiedAt ?: Long.MAX_VALUE }
                    items(entries, key = { it.identity }) { entry ->
                        val isOriginal = entry.identity == earliest?.identity
                        val isChecked = selectedCandidateIds.contains(entry.identity)
                        DuplicateMemberCard(
                            entry = entry,
                            isOriginal = isOriginal,
                            isChecked = isChecked,
                            formatBytes = ::bytes,
                            onToggle = {
                                if (isChecked) selectedCandidateIds.remove(entry.identity)
                                else selectedCandidateIds.add(entry.identity)
                            },
                            onOpen = { open(entry) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                val candidates = entries.filter { it.identity != earliest?.identity }
                                if (selectedCandidateIds.size == candidates.size) selectedCandidateIds.clear()
                                else {
                                    selectedCandidateIds.clear()
                                    selectedCandidateIds.addAll(candidates.map { it.identity })
                                }
                            }) {
                                val allSelected = selectedCandidateIds.size == entries.size - 1
                                Text(if (allSelected) "Deselect all" else "Select all candidates")
                            }
                            Text("${selectedCandidateIds.size} of ${entries.size - 1} selected", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                        }

                        if (selectedCandidateIds.isNotEmpty()) {
                            val selectedBytes = group.sizeBytes * selectedCandidateIds.size
                            KanoHeroButton(onClick = {
                                reviewConfirmation = selectedCandidateIds.size to selectedBytes
                            }) {
                                Text("Review selected (${selectedCandidateIds.size} candidates · ${bytes(selectedBytes)})")
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        KanoOutlinedButton({ duplicate = null }) { Text("Close duplicate review") }
                        Spacer(Modifier.navigationBarsPadding().height(16.dp))
                    }
                }
            }
        }
    }

    // Advisory Review Confirmation Dialog (Non-destructive)
    reviewConfirmation?.let { (count, recoverableBytes) ->
        AlertDialog(
            onDismissRequest = { reviewConfirmation = null },
            title = { Text("Candidate review summary", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$count duplicate copies selected for review.", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Text("Potential space recovery: ${bytes(recoverableBytes)}.", style = MaterialTheme.typography.titleMedium, color = colors.accent)
                    Text(
                        "Advisory review only · In Phase 2, Kano operates in review-only mode. No files will be modified or deleted. System confirmation will be required in Phase 3.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { reviewConfirmation = null }) {
                    Text("Understood")
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}

/** Rich item card matching the Kano design system and user mockup. */
@Composable
private fun StorageEntryCard(
    entry: StorageEntry,
    formatBytes: (Long?) -> String,
    onOpen: () -> Unit,
    onDetails: () -> Unit
) {
    val colors = KanoThemeColors
    val isVideo = entry.category in setOf("VIDEOS", "SCREEN_RECORDINGS") || entry.mime?.startsWith("video/") == true
    val isImage = entry.category in setOf("PHOTOS", "SCREENSHOTS") || entry.mime?.startsWith("image/") == true

    Surface(
        onClick = onDetails,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(0.75.dp, colors.glassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Thumbnail or Icon
                StorageMediaThumbnail(
                    entry = entry,
                    modifier = Modifier
                        .size(if (isVideo) 96.dp else 64.dp, 64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                // Details Column
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        entry.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Metadata row 1: Size · Resolution / Dimensions · Format
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatBytes(entry.sizeBytes), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.accent)
                        Text("·", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)

                        if (isVideo) {
                            val res = formatResolutionTag(entry.width, entry.height)
                            if (res != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = colors.accent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        res,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = colors.accent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text("·", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                            }
                            entry.durationMillis?.let {
                                Text(formatDuration(it), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        } else if (isImage) {
                            if (entry.width != null && entry.height != null) {
                                Text("${entry.width}×${entry.height}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Text("·", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                            }
                            Text(formatExtension(entry.name, entry.mime), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        } else {
                            Text(formatExtension(entry.name, entry.mime), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Text("·", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                            Text(storageLabel(entry.category), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }

                    // Metadata row 2: Location
                    entry.location?.let { loc ->
                        Text(
                            formatCleanLocation(loc),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Metadata row 3: Date
                    Text(
                        storageDate(entry.modifiedAt ?: entry.addedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }

            // Action buttons row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.heightIn(min = 40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onDetails,
                    modifier = Modifier.heightIn(min = 40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Details", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/** Card for each member in a duplicate group comparison. */
@Composable
private fun DuplicateMemberCard(
    entry: StorageEntry,
    isOriginal: Boolean,
    isChecked: Boolean,
    formatBytes: (Long?) -> String,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    val colors = KanoThemeColors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOriginal) colors.success.copy(alpha = 0.5f) else colors.glassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isOriginal) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Original copy", tint = colors.success, modifier = Modifier.size(20.dp))
                }
            }

            StorageMediaThumbnail(
                entry = entry,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            )

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isOriginal) "Original (Earliest)" else "Duplicate copy",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isOriginal) colors.success else colors.textSecondary
                    )
                }
                Text(entry.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatCleanLocation(entry.location), style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(storageDate(entry.modifiedAt ?: entry.addedAt), style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            }

            IconButton(onClick = onOpen, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "Open file", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Duplicate group summary card in Duplicates list. */
@Composable
private fun DuplicateGroupCard(
    group: StorageDuplicateTotal,
    formatBytes: (Long?) -> String,
    onReview: () -> Unit
) {
    val colors = KanoThemeColors
    Surface(
        onClick = onReview,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(0.75.dp, colors.glassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "${group.copies} matching copies",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    "${formatBytes(group.sizeBytes)} each · ${formatBytes(storageProduct(group.sizeBytes, group.copies))} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                if (group.localIdentityVerified) {
                    Text(
                        "${formatBytes(storageProduct(group.sizeBytes, group.copies - 1))} recoverable",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accent
                    )
                }
            }

            FilledTonalButton(
                onClick = onReview,
                modifier = Modifier.heightIn(min = 40.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Review", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Actionable recommendation card on Overview. */
@Composable
private fun StorageRecommendationCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = KanoThemeColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(0.75.dp, colors.glassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = actionLabel, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

/** Asynchronously loaded thumbnail with video duration overlay and icon fallbacks. */
@Composable
fun StorageMediaThumbnail(
    entry: StorageEntry,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = KanoThemeColors
    val isVideo = entry.category in setOf("VIDEOS", "SCREEN_RECORDINGS") || entry.mime?.startsWith("video/") == true
    val isImage = entry.category in setOf("PHOTOS", "SCREENSHOTS") || entry.mime?.startsWith("image/") == true

    var bitmap by remember(entry.uri) { mutableStateOf<Bitmap?>(null) }

    if (isImage || isVideo) {
        LaunchedEffect(entry.uri) {
            try {
                bitmap = GalleryThumbnails.load(context, Uri.parse(entry.uri), isVideo)
            } catch (_: Exception) {
                bitmap = null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isVideo) {
                entry.durationMillis?.let { duration ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Text(
                                formatDuration(duration),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            val fallbackIcon = when {
                isVideo -> Icons.Outlined.Videocam
                isImage -> Icons.Outlined.Image
                entry.category == "AUDIO" || entry.mime?.startsWith("audio/") == true -> Icons.Outlined.AudioFile
                entry.category == "APKS" || entry.name.endsWith(".apk", ignoreCase = true) -> Icons.Outlined.Android
                entry.category == "ARCHIVES" || entry.name.endsWith(".zip", ignoreCase = true) || entry.name.endsWith(".rar", ignoreCase = true) -> Icons.Outlined.FolderZip
                entry.category == "DOCUMENTS" || entry.name.endsWith(".pdf", ignoreCase = true) -> Icons.Outlined.Description
                else -> Icons.AutoMirrored.Outlined.InsertDriveFile
            }
            Icon(
                fallbackIcon,
                contentDescription = null,
                tint = if (isImage || isVideo) colors.textTertiary else colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Polished Scan status card. */
@Composable
private fun StorageScanStatusCard(scan: StorageScan?, totals: app.kano.data.StorageTotals?, active: Boolean) {
    val colors = KanoThemeColors
    val context = LocalContext.current
    fun bytes(v: Long?) = v?.let { Formatter.formatFileSize(context, it) } ?: "0 B"

    KanoGlassCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Storage scan", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            StatusChip(
                storageLabel(scan?.status ?: "FIRST_RUN"),
                tone = when (scan?.status) {
                    "ERROR", "PARTIAL_FAILURE" -> StatusTone.WARNING
                    "COMPLETED" -> StatusTone.SUCCESS
                    "SCANNING", "UPDATING" -> StatusTone.ACCENT
                    else -> StatusTone.NEUTRAL
                }
            )
        }

        if (active) {
            Spacer(Modifier.height(4.dp))
            if (scan?.phase == "HASHING" && scan.hashCandidates > 0) {
                LinearProgressIndicator(
                    progress = { (scan.hashed.toFloat() / scan.hashCandidates).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )
                Text("${scan.hashed} of ${scan.hashCandidates} hash candidates checked", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            } else {
                LinearProgressIndicator(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
            }
        }

        scan?.let {
            val countStr = totals?.let { t -> "${t.count} files indexed (${bytes(t.bytes)})" } ?: "${it.scanned} files read"
            Text(countStr, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(
                "Indexed: ${storageDate(it.finishedAt ?: it.startedAt)}" +
                    if (it.startedAt != null && it.finishedAt != null) " · Duration: ${((it.finishedAt - it.startedAt).coerceAtLeast(0) / 1000)}s" else "",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        } ?: Text("Tap below to start indexing authorized files.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

/** Icon resolver for storage categories. */
private fun categoryIcon(category: String): ImageVector = when (category) {
    "PHOTOS" -> Icons.Outlined.Photo
    "VIDEOS" -> Icons.Outlined.Videocam
    "SCREENSHOTS" -> Icons.Outlined.Screenshot
    "SCREEN_RECORDINGS" -> Icons.AutoMirrored.Outlined.ScreenShare
    "AUDIO" -> Icons.Outlined.AudioFile
    "DOCUMENTS" -> Icons.Outlined.Description
    "APKS" -> Icons.Outlined.Android
    "ARCHIVES" -> Icons.Outlined.FolderZip
    else -> Icons.Outlined.Folder
}

private fun formatResolutionTag(width: Int?, height: Int?): String? {
    if (width == null || height == null || width <= 0 || height <= 0) return null
    val maxDim = maxOf(width, height)
    val minDim = minOf(width, height)
    return when {
        maxDim >= 3840 || minDim >= 2160 -> "4K"
        maxDim >= 2560 || minDim >= 1440 -> "2K"
        maxDim >= 1920 || minDim >= 1080 -> "1080p"
        maxDim >= 1280 || minDim >= 720 -> "720p"
        else -> "${width}×${height}"
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}

private fun formatExtension(name: String, mime: String?): String {
    val ext = name.substringAfterLast('.', "").uppercase(Locale.ROOT)
    if (ext.isNotBlank() && ext.length <= 5) return ext
    val mimeSub = mime?.substringAfter('/')?.substringBefore(';')?.uppercase(Locale.ROOT)
    return mimeSub?.take(5) ?: "FILE"
}

private fun formatCleanLocation(location: String?): String {
    if (location.isNullOrBlank()) return "Storage"
    return location.removePrefix("/storage/emulated/0/").removePrefix("/storage/").trim('/')
}

private fun storageDate(value: Long?): String = value?.takeIf { it > 0 }?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) } ?: "Unavailable"

private fun storageLabel(value: String): String = when (value) {
    "APKS" -> "APKs"
    "KANO_DATA" -> "Kano data"
    "SCREEN_RECORDINGS" -> "Screen recordings"
    else -> value.lowercase(Locale.ROOT).replace('_', ' ').replaceFirstChar { it.uppercase(Locale.ROOT) }
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

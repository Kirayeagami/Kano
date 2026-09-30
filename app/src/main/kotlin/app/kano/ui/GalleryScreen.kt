package app.kano.ui

import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.*
import app.kano.platform.*
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GalleryScreen(model: KanoViewModel, openCamera: () -> Unit, openSavedDocuments: () -> Unit, footerInset: Dp = 0.dp) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val state by model.gallery.collectAsStateWithLifecycle()
    val filter by model.galleryFilter.collectAsStateWithLifecycle()
    val query by model.galleryQuery.collectAsStateWithLifecycle()
    val offset by model.galleryOffset.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<GalleryItem?>(null) }
    var tools by remember { mutableStateOf(false) }
    var albums by remember { mutableStateOf(false) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { model.refreshGallery() }
    fun appSettings() {
        try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${context.packageName}"))) }
        catch (_: Exception) { model.message.value = "Android app settings unavailable." }
    }
    DisposableEffect(owner, model) {
        val lifecycle = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { GalleryThumbnails.clear(); selected = null; model.refreshGallery() }
            if (event == Lifecycle.Event.ON_STOP) { selected = null; GalleryThumbnails.clear() }
        }
        owner.lifecycle.addObserver(lifecycle)
        val handler = Handler(Looper.getMainLooper())
        val refresh = Runnable { if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) model.refreshGallery() }
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) { GalleryThumbnails.clear(); handler.removeCallbacks(refresh); handler.postDelayed(refresh, 300) }
        }
        context.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
        context.contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer)
        model.refreshGallery()
        onDispose { owner.lifecycle.removeObserver(lifecycle); context.contentResolver.unregisterContentObserver(observer); handler.removeCallbacks(refresh) }
    }
    val ready = state as? GalleryState.Ready
    LazyVerticalGrid(GridCells.Adaptive(104.dp), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + footerInset),
        horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Kano Gallery", style = MaterialTheme.typography.headlineMedium, color = KanoThemeColors.textPrimary, modifier = Modifier.weight(1f))
                IconButton(openCamera) { Icon(Icons.Outlined.CameraAlt, "Camera", tint = KanoThemeColors.iconPrimary) }
                Box {
                    IconButton({ tools = true }) { Icon(Icons.Outlined.MoreHoriz, "Gallery tools", tint = KanoThemeColors.iconPrimary) }
                    DropdownMenu(tools, { tools = false }) {
                        DropdownMenuItem(text = { Text("Manage media access") }, onClick = { tools = false; permissions.launch(model.galleryPermissions()) })
                        DropdownMenuItem(text = { Text("Android app settings") }, onClick = { tools = false; appSettings() })
                        DropdownMenuItem(text = { Text("Saved document index") }, onClick = { tools = false; openSavedDocuments() })
                        DropdownMenuItem(text = { Text(if (albums) "Timeline" else "Albums on this page") }, onClick = { tools = false; albums = !albums })
                    }
                }
            }
            Text(when (val current = state) { is GalleryState.Ready -> current.access.label; is GalleryState.NoAccess -> current.access.label; GalleryState.Failed -> "Media provider unavailable"; else -> "Checking media access…" }, style = MaterialTheme.typography.labelMedium, color = KanoThemeColors.textSecondary)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(query, model::searchGallery, modifier = Modifier.fillMaxWidth(), label = { Text("Search gallery filenames") }, singleLine = true, shape = MaterialTheme.shapes.large)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GalleryFilter.entries.forEach { choice -> FilterChip(filter == choice, { model.changeGalleryFilter(choice) }, label = { Text(choice.label) }) }
            }
        }
        when (state) {
            GalleryState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { KanoStateSurface("Reading media", "Querying the accessible library.", true) }
            GalleryState.Failed -> item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KanoStateSurface("Gallery unavailable", "Could not read the media provider.")
                KanoOutlinedButton({ model.refreshGallery() }) { Text("Retry gallery query") }
                }
            }
            is GalleryState.NoAccess -> item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KanoStateSurface("Choose what Kano can see", "Photo/video permission enables local browsing and image analysis. Android supports full or selected access. Your original files are kept.")
                KanoHeroButton({ permissions.launch(model.galleryPermissions()) }) { Text("Manage media access") }
                KanoOutlinedButton(openSavedDocuments) { Text("Saved documents") }
                }
            }
            is GalleryState.Ready -> {
                ready?.let { current ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val sizes = current.page.items.mapNotNull { it.size }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("This page · ${current.page.items.count { !it.isVideo }} photos · ${current.page.items.count { it.isVideo }} videos", style = MaterialTheme.typography.labelSmall, color = KanoThemeColors.textSecondary)
                        Text(Formatter.formatFileSize(context, sizes.sum()) + " reported" + if (sizes.size != current.page.items.size) " · some sizes unavailable" else "", style = MaterialTheme.typography.labelSmall, color = KanoThemeColors.textTertiary)
                        }
                    }
                    if (current.page.items.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { KanoStateSurface("No matching media", "This view has no permitted items. Change the filter or selection.") }
                    current.page.items.groupBy { if (albums) it.album ?: "Album unavailable" else DateFormat.getDateInstance().format(Date(it.takenAt ?: it.dateAddedSeconds * 1000)) }.forEach { (label, assets) ->
                        item(span = { GridItemSpan(maxLineSpan) }, key = "group-$label") { Text(label, style = MaterialTheme.typography.titleSmall, color = KanoThemeColors.textPrimary, modifier = Modifier.padding(top = 8.dp)) }
                        items(assets, key = { it.uri.toString() }) { asset ->
                            Column(Modifier.clip(MaterialTheme.shapes.small).clickable { selected = asset }) {
                                MediaThumbnail(asset, Modifier.fillMaxWidth().aspectRatio(1f), current.page.capturedAt)
                                if (asset.isVideo) {
                                    Surface(
                                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f),
                                        shape = MaterialTheme.shapes.extraSmall,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            asset.durationMillis?.let { "${it / 60000}:${((it / 1000) % 60).toString().padStart(2, '0')}" } ?: "Video",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KanoOutlinedButton({ model.nextGalleryPage(-1) }, enabled = offset > 0) { Text("Previous page") }
                            KanoOutlinedButton({ model.nextGalleryPage(1) }, enabled = current.page.hasMore) { Text("Next page") }
                        }
                        Text("Updated " + DateFormat.getTimeInstance().format(Date(current.page.capturedAt)) + " · bounded 60-item page", style = MaterialTheme.typography.labelSmall, color = KanoThemeColors.textTertiary)
                        }
                    }
                }
            }
        }
    }
    selected?.let { asset ->
        ModalBottomSheet(onDismissRequest = { selected = null }, containerColor = KanoThemeColors.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(asset.name, style = MaterialTheme.typography.titleLarge, color = KanoThemeColors.textPrimary)
                MediaThumbnail(asset, Modifier.fillMaxWidth().aspectRatio(1.2f).clip(MaterialTheme.shapes.large), ready?.page?.capturedAt ?: 0)
                if (!asset.isVideo) KanoHeroButton({ selected = null; model.runLocalVision(asset.uri) }, enabled = !busy) { Text("Analyze with Kano Vision") }
                KanoOutlinedButton({
                    try { context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(asset.uri, asset.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
                    catch (_: Exception) { model.message.value = "No app can open this item, or access was revoked." }
                }) { Text(if (asset.isVideo) "Play video" else "Open full image") }
                FactRow("Format / size", asset.mime + " · " + (asset.size?.let { Formatter.formatFileSize(context, it) } ?: "Size unavailable"))
                FactRow("Resolution", if (asset.width != null && asset.height != null) "${asset.width} × ${asset.height}" else "Unavailable")
                FactRow("Album", asset.album ?: "Unavailable")
                FactRow("Favorite", asset.favorite?.let { if (it) "Yes" else "No" } ?: "Unavailable")
                Text("Original-file cleanup is unavailable until reviewed extraction and OS confirmation are verified.", style = MaterialTheme.typography.bodySmall, color = KanoThemeColors.textTertiary)
                TextButton({ selected = null }) { Text("Close") }
            }
        }
    }
}
@Composable
fun MediaThumbnail(asset: GalleryItem, modifier: Modifier, revision: Long) {
    val context = LocalContext.current
    var loaded by remember(asset.uri, revision) { mutableStateOf(false) }
    val bitmap by produceState<android.graphics.Bitmap?>(null, asset.uri, revision) {
        value = GalleryThumbnails.load(context, asset.uri, asset.isVideo); loaded = true
    }
    val opacity by animateFloatAsState(if (bitmap != null) 1f else 0f, tween(if (LocalMotionEnabled.current) KanoMotion.CONTENT else 0), label = "gallery image reveal")
    if (bitmap != null) Image(bitmap!!.asImageBitmap(), asset.name, modifier, contentScale = ContentScale.Crop, alpha = opacity)
    else Surface(modifier, color = KanoThemeColors.surfaceElevated) {
        Box(contentAlignment = Alignment.Center) { Text(if (!loaded) "Loading…" else "Preview unavailable", style = MaterialTheme.typography.labelSmall, color = KanoThemeColors.textTertiary, modifier = Modifier.padding(8.dp)) }
    }
}

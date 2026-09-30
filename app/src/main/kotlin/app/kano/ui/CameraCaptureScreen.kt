package app.kano.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

private val capturePublishLock = Mutex()
private fun discardPrivateCapture(path: String) {
    File(path).delete()
    File(path + ".destination").delete()
}

/** Lifecycle-bound CameraX with a real capture review; publishing requires an explicit tap. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraCaptureScreen(onCaptured: (Uri) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    fun granted() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var cameraGranted by remember { mutableStateOf(granted()) }
    var front by rememberSaveable { mutableStateOf(false) }
    var flash by rememberSaveable { mutableStateOf(false) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var canSwitch by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var retryToken by remember { mutableIntStateOf(0) }
    var screenActive by remember { mutableStateOf(true) }
    DisposableEffect(Unit) { onDispose { screenActive = false } }
    BackHandler(enabled = saving) { /* Wait for the in-flight capture or publish to finish. */ }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraGranted = it
        error = if (it) null else "Camera access was not granted. Change it in Android app settings to continue."
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) cameraGranted = granted() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val pendingUri = pendingPath?.let { FileProvider.getUriForFile(context, "${context.packageName}.files", File(it)) }
    BackHandler(enabled = pendingPath != null && !saving) {
        pendingPath?.let(::discardPrivateCapture); pendingPath = null; onClose()
    }
    if (pendingUri != null) {
        val preview by produceState<android.graphics.Bitmap?>(null, pendingUri) {
            value = app.kano.platform.GalleryThumbnails.load(context, pendingUri, false)
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Review capture", style = MaterialTheme.typography.headlineSmall, color = KanoThemeColors.textPrimary)
            if (preview != null) Image(preview!!.asImageBitmap(), contentDescription = "Captured image", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).aspectRatio(.85f))
            else Text("Loading captured image…", style = MaterialTheme.typography.labelMedium, color = KanoThemeColors.textSecondary)
            Text(if (Build.VERSION.SDK_INT >= 29) "Use saves this photo to Pictures/Kano, then starts local analysis." else "Use keeps this photo in Kano's private storage, then starts local analysis.", color = KanoThemeColors.textSecondary)
            error?.let { Text(it, color = KanoThemeColors.error) }
            KanoHeroButton(onClick = {
                saving = true
                scope.launch {
                    try {
                        val source = File(pendingPath!!)
                        val receipt = File(source.absolutePath + ".destination")
                        val uri = withContext(Dispatchers.IO) {
                            capturePublishLock.withLock {
                            if (Build.VERSION.SDK_INT >= 29) {
                                val resolver = context.contentResolver
                                // Retain the private capture and owned destination through rotation.
                                // A cancelled UI can retry analysis without publishing a second photo.
                                val existing = receipt.takeIf { it.exists() }?.readText()?.let(Uri::parse)
                                if (existing != null && existing.authority == "media") {
                                    val published = resolver.query(existing, arrayOf(MediaStore.Images.Media.IS_PENDING, MediaStore.Images.Media.SIZE), null, null, null)?.use {
                                        it.moveToFirst() && it.getInt(0) == 0 && it.getLong(1) > 0
                                    } ?: false
                                    if (published) return@withLock existing
                                    resolver.delete(existing, null, null)
                                    receipt.delete()
                                }
                                val values = ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, "Kano-${System.currentTimeMillis()}.jpg")
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kano")
                                    put(MediaStore.Images.Media.IS_PENDING, 1)
                                }
                                val target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("No media destination")
                                try {
                                    receipt.writeText(target.toString())
                                    resolver.openOutputStream(target)?.use { output -> source.inputStream().use { it.copyTo(output) } } ?: error("No output")
                                    check(resolver.update(target, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null) == 1)
                                    target
                                } catch (failure: Exception) { resolver.delete(target, null, null); receipt.delete(); throw failure }
                            } else pendingUri
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 29) discardPrivateCapture(source.absolutePath)
                        pendingPath = null
                        onCaptured(uri)
                    } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Could not save this capture. Review is kept for retry." }
                    finally { saving = false }
                }
            }, enabled = !saving) { Text(if (saving) "Saving photo…" else "Use photo and analyze") }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KanoOutlinedButton(onClick = { discardPrivateCapture(pendingPath!!); pendingPath = null; error = null }, enabled = !saving) { Text("Retake") }
                KanoOutlinedButton(onClick = { discardPrivateCapture(pendingPath!!); pendingPath = null; onClose() }, enabled = !saving) { Text("Discard") }
            }
        }
        return
    }

    if (!cameraGranted) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Kano Vision", style = MaterialTheme.typography.headlineMedium, color = KanoThemeColors.textPrimary)
            Text("Camera permission is required for preview and capture. Analysis stays on this device.", color = KanoThemeColors.textSecondary, modifier = Modifier.padding(vertical = 12.dp))
            KanoHeroButton(onClick = { permission.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
            if (error != null) KanoOutlinedButton(onClick = {
                try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
                catch (_: Exception) { error = "Android app settings unavailable." }
            }) { Text("Android camera settings") }
            KanoOutlinedButton(onClick = onClose) { Text("Back") }
            error?.let { Text(it, color = KanoThemeColors.error) }
        }
        return
    }

    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    var shutterFlash by remember { mutableStateOf(false) }
    val flashAlpha by androidx.compose.animation.core.animateFloatAsState(
        if (shutterFlash) 0.85f else 0f,
        androidx.compose.animation.core.tween(120),
        finishedListener = { shutterFlash = false },
        label = "shutter flash"
    )
    DisposableEffect(owner, previewView, front, retryToken) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed) try {
                provider = future.get()
                val hasFront = provider!!.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                val hasBack = provider!!.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                check(hasFront || hasBack)
                val useFront = if (front) hasFront else !hasBack
                val selector = if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                canSwitch = hasFront && hasBack
                val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                    .setAspectRatioStrategy(androidx.camera.core.resolutionselector.AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build().also { it.surfaceProvider = previewView.surfaceProvider }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setResolutionSelector(resolutionSelector)
                    .setJpegQuality(95)
                    .build()
                provider!!.unbindAll()
                val camera = provider!!.bindToLifecycle(owner, selector, preview, capture)
                hasFlash = camera.cameraInfo.hasFlashUnit()
                imageCapture = capture
                error = null
            } catch (_: Exception) { imageCapture = null; error = "Camera unavailable. Check Android camera privacy controls or retry." }
        }, ContextCompat.getMainExecutor(context))
        onDispose { disposed = true; imageCapture = null; provider?.unbindAll() }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (flashAlpha > 0f) {
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White.copy(alpha = flashAlpha)))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip("Kano Vision · ${if (front) "Front" else "Rear"}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canSwitch) KanoOutlinedButton(onClick = { front = !front }, enabled = !saving) { Text("Switch lens") }
                if (hasFlash) KanoOutlinedButton(onClick = { flash = !flash }, enabled = !saving) { Text(if (flash) "Flash on" else "Flash off") }
            }
        }
        KanoGlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(max = maxHeight * .55f)
                .verticalScroll(rememberScrollState())
        ) {
            error?.let { Text(it, color = KanoThemeColors.error) }
            if (error != null) KanoOutlinedButton(onClick = { retryToken++ }, enabled = !saving) { Text("Retry camera") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KanoOutlinedButton(onClick = onClose, enabled = !saving) { Text("Cancel") }
                androidx.compose.material3.Surface(
                    onClick = {
                        val capture = imageCapture ?: return@Surface
                        capture.targetRotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0
                        capture.flashMode = if (flash && hasFlash) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                        val file = File(context.filesDir, "kano-captures/${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
                        saving = true
                        shutterFlash = true
                        capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    if (!screenActive) { file.delete(); return }
                                    saving = false; pendingPath = file.absolutePath
                                }
                                override fun onError(exception: ImageCaptureException) { saving = false; file.delete(); error = "Capture failed. Try again." }
                            })
                    },
                    enabled = imageCapture != null && !saving,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = KanoThemeColors.surface,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(3.dp, KanoThemeColors.accent.copy(alpha = .7f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (saving) "Capturing…" else "Capture",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = KanoThemeColors.textPrimary
                        )
                    }
                }
                if (canSwitch) KanoOutlinedButton(onClick = { front = !front }, enabled = !saving) { Text("Switch lens") }
                else Spacer(Modifier.width(64.dp))
            }
        }
    }
}

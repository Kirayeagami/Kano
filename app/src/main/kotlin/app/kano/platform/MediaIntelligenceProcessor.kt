package app.kano.platform

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import app.kano.core.VisionPolicy
import app.kano.data.ExtractionType
import app.kano.data.KnowledgeType
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class VisionCandidate(val title: String, val detail: String, val type: KnowledgeType,
    val extraction: ExtractionType, val url: String? = null)
data class MediaIntelligenceResult(
    val sourceUri: String,
    val digest: String,
    val extractedText: String?,
    val qrPayload: String?,
    val candidates: List<VisionCandidate>,
    val isSensitiveRedacted: Boolean,
    val issues: List<String>,
) { val recognizedEntities: Int get() = candidates.size }

/** One bounded local engine for gallery, camera, Style and Care. Results require explicit review. */
class MediaIntelligenceProcessor(context: Context) {
    private val resolver = context.contentResolver

    suspend fun processImageUri(uri: Uri): MediaIntelligenceResult = withContext(Dispatchers.IO) {
        require(uri.scheme == "content")
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(16 * 1024)
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= MAX_ENCODED_BYTES) { "Image exceeds the scan byte limit" }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("Image unavailable")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unsupported image" }
        var sample = 1
        while (bounds.outWidth / sample > MAX_DIMENSION || bounds.outHeight / sample > MAX_DIMENSION) sample *= 2
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Image unavailable")
        val exif = runCatching { ExifInterface(bytes.inputStream()) }.getOrNull()
        val matrix = Matrix().apply {
            postRotate((exif?.rotationDegrees ?: 0).toFloat())
            if (exif?.isFlipped == true) postScale(-1f, 1f)
        }
        val oriented = if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val image = InputImage.fromBitmap(oriented, 0)
        val qr = BarcodeScanning.getClient()
        val ocr = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val issues = mutableListOf<String>()
        var text: String? = null
        var payload: String? = null
        var sensitiveQr = false
        val candidates = mutableListOf<VisionCandidate>()
        try {
            try {
                val results = qr.process(image).awaitLocal()
                for (barcode in results.take(20)) {
                    val value = barcode.rawValue?.take(8_000) ?: continue
                    payload = value
                    sensitiveQr = sensitiveQr || VisionPolicy.hasSensitiveSignal(value)
                    VisionPolicy.publicWebUrl(value)?.let { url ->
                        candidates += VisionCandidate("Website from QR", "Decoded web address; identity has not been verified.",
                            KnowledgeType.WEBSITE, ExtractionType.QR_CODE, url)
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { issues += "QR recognition failed" }
            try { text = ocr.process(image).awaitLocal().text.take(8_000).takeIf { it.isNotBlank() } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { issues += "Text recognition failed" }
            currentCoroutineContext().ensureActive()
            val sensitive = VisionPolicy.hasSensitiveSignal(text.orEmpty()) || sensitiveQr
            if (!sensitive) {
                VisionPolicy.webUrls(text.orEmpty()).forEach { url ->
                    candidates += VisionCandidate("Website from text", "OCR web address candidate; check for recognition errors.",
                        KnowledgeType.WEBSITE, ExtractionType.OCR, url)
                }
                if (!text.isNullOrBlank()) candidates += VisionCandidate("Text excerpt", text!!.take(300),
                    KnowledgeType.UNKNOWN, ExtractionType.OCR)
            } else candidates.clear()
            // Store a digest of the exact decoded input, not private content in diagnostics.
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            MediaIntelligenceResult(uri.toString(), digest, if (sensitive) null else text,
                if (sensitive) null else payload, candidates.distinctBy { it.url ?: it.detail }, sensitive, issues)
        } finally {
            qr.close(); ocr.close()
            // No bitmap cache retains originals. Native tasks may still reference input after cancellation.
        }
    }

    private suspend fun <T> Task<T>.awaitLocal(): T = suspendCancellableCoroutine { continuation ->
        val direct = Executor { it.run() }
        addOnSuccessListener(direct) { if (continuation.isActive) continuation.resume(it) }
        addOnFailureListener(direct) { if (continuation.isActive) continuation.resumeWithException(it) }
        addOnCanceledListener(direct) { continuation.cancel() }
    }
    companion object { const val MAX_ENCODED_BYTES = 20 * 1024 * 1024; const val MAX_DIMENSION = 1600 }
}

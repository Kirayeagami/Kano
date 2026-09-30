package app.kano

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import app.kano.platform.MediaIntelligenceProcessor
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

/** Synthetic fixtures exercise the actual bundled ML Kit engines, never user media. */
class LocalVisionTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun image(bitmap: Bitmap, action: (android.net.Uri) -> Unit) {
        val file = File(context.filesDir, "kano-captures/KANO_TEST_ONLY-${UUID.randomUUID()}.png").apply { parentFile?.mkdirs() }
        try {
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            action(FileProvider.getUriForFile(context, "${context.packageName}.files", file))
        } finally { file.delete(); bitmap.recycle() }
    }
    private fun qr(payload: String): Bitmap {
        val matrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 640, 640)
        return Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (y in 0 until 640) for (x in 0 until 640) bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    @Test fun realQrRecognitionProducesAReviewCandidateWithoutSaving() = runBlocking {
        image(qr("https://developer.android.com")) { uri ->
            runBlocking {
                val result = MediaIntelligenceProcessor(context).processImageUri(uri)
                assertTrue(result.candidates.any { it.url == "https://developer.android.com" })
                assertFalse(result.isSensitiveRedacted)
            }
        }
    }
    @Test fun secretQrNeverProducesPersistableCandidates() = runBlocking {
        image(qr("WIFI:S:KANO_TEST_ONLY;P:test-credential;;")) { uri ->
            runBlocking {
                val result = MediaIntelligenceProcessor(context).processImageUri(uri)
                assertTrue(result.isSensitiveRedacted)
                assertNull(result.qrPayload)
                assertTrue(result.candidates.isEmpty())
            }
        }
    }
    @Test fun realOcrRecognizesSyntheticText() = runBlocking {
        val bitmap = Bitmap.createBitmap(900, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 58f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("KANO TEST ONLY", 36f, 90f, paint)
        canvas.drawText("Local study note", 36f, 190f, paint)
        image(bitmap) { uri -> runBlocking {
            val result = MediaIntelligenceProcessor(context).processImageUri(uri)
            assertTrue(result.extractedText?.contains("KANO", true) == true)
            assertTrue(result.candidates.any { it.title == "Text excerpt" })
        } }
    }
    @Test fun malformedAndOversizedImagesFailWithoutSuccessResult() = runBlocking {
        val file = File(context.filesDir, "kano-captures/KANO_TEST_ONLY-bad.bin").apply { parentFile?.mkdirs() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        try {
            file.writeText("not an image")
            try { MediaIntelligenceProcessor(context).processImageUri(uri); fail("Malformed image accepted") } catch (_: IllegalArgumentException) {}
            file.outputStream().use { output -> repeat(21) { output.write(ByteArray(1024 * 1024)) } }
            try { MediaIntelligenceProcessor(context).processImageUri(uri); fail("Oversized image accepted") } catch (_: IllegalArgumentException) {}
        } finally { file.delete() }
    }
}

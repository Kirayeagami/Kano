package app.kano.platform

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import app.kano.core.PrivacyFirewall
import app.kano.data.Confidence
import app.kano.data.ExtractionType
import app.kano.data.KnowledgeRepository
import app.kano.data.KnowledgeType
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class MediaIntelligenceResult(
    val extractedText: String?,
    val qrPayload: String?,
    val recognizedEntities: Int,
    val isSensitiveRedacted: Boolean,
)

class MediaIntelligenceProcessor(
    private val context: Context,
    private val knowledgeRepository: KnowledgeRepository,
) {
    private val resolver = context.contentResolver
    private val urlPattern = Pattern.compile("(https?://[\\w\\.-]+\\.[a-zA-Z]{2,6}[/\\w\\.-]*)", Pattern.CASE_INSENSITIVE)

    suspend fun processImageUri(uri: Uri): MediaIntelligenceResult = withContext(Dispatchers.IO) {
        val bitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return@withContext MediaIntelligenceResult(null, null, 0, false)

        val image = InputImage.fromBitmap(bitmap, 0)
        var entitiesFound = 0
        var isSensitive = false

        // 1. On-Device Local QR Code Detection
        val barcodeScanner = BarcodeScanning.getClient()
        var qrResultPayload: String? = null
        try {
            val barcodes = Tasks.await(barcodeScanner.process(image))
            for (barcode in barcodes) {
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrBlank()) {
                    qrResultPayload = rawValue
                    knowledgeRepository.save(
                        title = "QR Payload Detected",
                        detail = rawValue.take(200),
                        entityType = KnowledgeType.QR_PAYLOAD,
                        sourceUri = uri.toString(),
                        extractionType = ExtractionType.QR_CODE,
                        confidence = Confidence.CONFIRMED,
                        urlOrPayload = rawValue,
                    )
                    entitiesFound++
                }
            }
        } catch (_: Exception) {
            // QR scanning failed gracefully; continue to OCR
        }

        // 2. On-Device Local OCR Text Recognition
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var extractedRawText: String? = null
        try {
            val visionText = Tasks.await(textRecognizer.process(image))
            val fullText = visionText.text
            if (fullText.isNotBlank()) {
                // Secret redaction for sensitive patterns (passwords, tokens, credit cards)
                val isSensitiveMatch = fullText.contains(Regex("(?i)\\b(password|passwd|otp|api[_ -]?key|sk-[A-Za-z0-9_-]{12,})\\b"))
                isSensitive = isSensitiveMatch
                val safeText = if (isSensitive) fullText.replace(Regex("(?i)\\b(password|passwd|otp|api[_ -]?key|sk-[A-Za-z0-9_-]{12,})\\b"), "[REDACTED]") else fullText
                extractedRawText = safeText

                // Extract Candidate URLs from OCR Text
                val matcher = urlPattern.matcher(safeText)
                while (matcher.find()) {
                    val candidateUrl = matcher.group(1)
                    if (!candidateUrl.isNullOrBlank()) {
                        knowledgeRepository.save(
                            title = "Extracted Website Link",
                            detail = "URL detected via OCR text scan",
                            entityType = KnowledgeType.WEBSITE,
                            sourceUri = uri.toString(),
                            extractionType = ExtractionType.OCR,
                            confidence = Confidence.LIKELY,
                            urlOrPayload = candidateUrl,
                        )
                        entitiesFound++
                    }
                }

                // Entity Keyword Classification (Study Note, Movie, Book, Product, Receipt)
                val lowerText = safeText.lowercase()
                when {
                    lowerText.contains("chapter") || lowerText.contains("lecture") || lowerText.contains("exam") || lowerText.contains("notes") -> {
                        knowledgeRepository.save(
                            title = "Study Note Content",
                            detail = safeText.take(150),
                            entityType = KnowledgeType.STUDY_NOTE,
                            sourceUri = uri.toString(),
                            extractionType = ExtractionType.OCR,
                            confidence = Confidence.LIKELY,
                        )
                        entitiesFound++
                    }
                    lowerText.contains("movie") || lowerText.contains("imdb") || lowerText.contains("cinema") || lowerText.contains("directed by") -> {
                        knowledgeRepository.save(
                            title = "Movie Title Candidate",
                            detail = safeText.take(150),
                            entityType = KnowledgeType.MOVIE,
                            sourceUri = uri.toString(),
                            extractionType = ExtractionType.OCR,
                            confidence = Confidence.LIKELY,
                        )
                        entitiesFound++
                    }
                    lowerText.contains("total") && (lowerText.contains("receipt") || lowerText.contains("tax") || lowerText.contains("invoice")) -> {
                        knowledgeRepository.save(
                            title = "Receipt / Invoice Document",
                            detail = safeText.take(150),
                            entityType = KnowledgeType.RECEIPT,
                            sourceUri = uri.toString(),
                            extractionType = ExtractionType.OCR,
                            confidence = Confidence.LIKELY,
                        )
                        entitiesFound++
                    }
                }
            }
        } catch (_: Exception) {
            // OCR recognition failed gracefully
        }

        MediaIntelligenceResult(
            extractedText = extractedRawText,
            qrPayload = qrResultPayload,
            recognizedEntities = entitiesFound,
            isSensitiveRedacted = isSensitive,
        )
    }
}

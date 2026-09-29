package app.kano.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class KnowledgeType(val label: String) {
    WEBSITE("Website"),
    PRODUCT("Product"),
    APP("App"),
    MOVIE("Movie"),
    SERIES("Series"),
    SONG("Song"),
    BOOK("Book"),
    STUDY_NOTE("Study Note"),
    PROJECT("Project"),
    RECEIPT("Receipt"),
    QR_PAYLOAD("QR Payload"),
    UNKNOWN("Unknown"),
}

enum class ExtractionType {
    OCR,
    QR_CODE,
    USER_ENTRY,
}

enum class Confidence {
    CONFIRMED,
    LIKELY,
    UNCERTAIN,
}

class KnowledgeRepository(private val database: KanoDatabase) {
    val allEntities: Flow<List<KnowledgeRecord>> = database.knowledge().observeAll()
    val totalCount: Flow<Int> = database.knowledge().observeCount()

    fun entitiesByType(type: KnowledgeType): Flow<List<KnowledgeRecord>> {
        return database.knowledge().observeByType(type.name)
    }

    suspend fun save(
        title: String,
        detail: String,
        entityType: KnowledgeType,
        sourceUri: String,
        extractionType: ExtractionType,
        confidence: Confidence,
        urlOrPayload: String? = null,
        id: String? = null,
    ) {
        val record = KnowledgeRecord(
            id = id ?: UUID.randomUUID().toString(),
            entityType = entityType.name,
            title = title.trim().take(160),
            detail = detail.trim().take(300),
            urlOrPayload = urlOrPayload?.trim()?.take(500),
            sourceUri = sourceUri,
            extractionType = extractionType.name,
            confidence = confidence.name,
            createdAt = System.currentTimeMillis(),
        )
        database.knowledge().insert(record)
    }

    suspend fun delete(id: String) {
        database.knowledge().delete(id)
    }

    suspend fun clear() {
        database.knowledge().clear()
    }
}

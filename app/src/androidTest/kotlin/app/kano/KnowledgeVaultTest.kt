package app.kano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.kano.data.Confidence
import app.kano.data.ExtractionType
import app.kano.data.KanoDatabase
import app.kano.data.KnowledgeRepository
import app.kano.data.KnowledgeType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnowledgeVaultTest {
    private lateinit var db: KanoDatabase
    private lateinit var repository: KnowledgeRepository

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, KanoDatabase::class.java).build()
        repository = KnowledgeRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun saveAndRetrieveKnowledgeEntity() = runBlocking {
        repository.save(
            title = "Sample Website Link",
            detail = "URL extracted via OCR scan",
            entityType = KnowledgeType.WEBSITE,
            sourceUri = "content://media/external/images/media/1",
            extractionType = ExtractionType.OCR,
            confidence = Confidence.CONFIRMED,
            urlOrPayload = "https://developer.android.com",
        )

        val items = repository.allEntities.first()
        assertEquals(1, items.size)

        val entity = items.first()
        assertEquals("Sample Website Link", entity.title)
        assertEquals("WEBSITE", entity.entityType)
        assertEquals("https://developer.android.com", entity.urlOrPayload)
        assertEquals("CONFIRMED", entity.confidence)
    }

    @Test
    fun deleteKnowledgeEntity() = runBlocking {
        repository.save(
            title = "QR Code Payload",
            detail = "Reviewed website",
            entityType = KnowledgeType.QR_PAYLOAD,
            sourceUri = "content://media/external/images/media/2",
            extractionType = ExtractionType.QR_CODE,
            confidence = Confidence.CONFIRMED,
            urlOrPayload = "https://example.com",
            id = "entity-qr-123",
        )

        val countBefore = repository.totalCount.first()
        assertEquals(1, countBefore)

        repository.delete("entity-qr-123")

        val countAfter = repository.totalCount.first()
        assertEquals(0, countAfter)
    }
}

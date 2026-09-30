package app.kano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.kano.data.KanoDatabase
import app.kano.data.MediaRecord
import app.kano.data.MediaRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaIntelligenceTest {
    private lateinit var db: KanoDatabase
    private lateinit var repository: MediaRepository

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, KanoDatabase::class.java).build()
        repository = MediaRepository(context, db.media())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun findDuplicatesGroupsMatchingHashes() = runBlocking {
        val sampleHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val record1 = MediaRecord(uri = "content://media/external/images/media/100", name = "Photo1.jpg", state = "INDEXED", sha256 = sampleHash)
        val record2 = MediaRecord(uri = "content://media/external/images/media/101", name = "Photo1_Copy.jpg", state = "INDEXED", sha256 = sampleHash)
        val record3 = MediaRecord(uri = "content://media/external/images/media/102", name = "Photo2.jpg", state = "INDEXED", sha256 = "different_hash_abc")

        db.media().add(record1)
        db.media().add(record2)
        db.media().add(record3)

        val duplicateGroups = repository.findDuplicates()
        assertEquals(1, duplicateGroups.size)
        assertEquals(sampleHash, duplicateGroups.first().sha256)
        assertEquals(2, duplicateGroups.first().items.size)
    }

    @Test
    fun forgetRecordHandlesRevokedOrUnaccessibleUri() = runBlocking {
        val record = MediaRecord(uri = "content://media/external/images/media/999", name = "Revoked.jpg", state = "INDEXED", sha256 = "some_hash")
        db.media().add(record)

        val result = repository.forgetRecord(record)
        // Since URI permission is not persistable on synthetic content URI, returns false and marks ACCESS_REVOKED
        assertFalse(result)

        val saved = db.media().all().firstOrNull { it.uri == record.uri }
        assertEquals("ACCESS_REVOKED", saved?.state)
    }
}

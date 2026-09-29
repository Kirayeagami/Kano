package app.kano

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.kano.data.KanoDatabase
import app.kano.data.MediaRecord
import app.kano.data.MediaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test fun duplicateImportPreservesAnalysisAndSearchIsLiteral() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KanoDatabase::class.java).build()
        try {
            val dao = db.media()
            dao.add(MediaRecord("content://test/1", "100%_image", state = "INDEXED"))
            dao.add(MediaRecord("content://test/1"))
            dao.add(MediaRecord("content://test/2", "100xximage"))
            assertEquals(2, dao.count())
            val rows = dao.observe(MediaRepository.searchPattern("%_"), 25, 0).first()
            assertEquals(1, rows.size)
            assertEquals("INDEXED", rows.single().state)
            dao.clear()
            assertEquals(0, dao.count())
        } finally { db.close() }
    }
}

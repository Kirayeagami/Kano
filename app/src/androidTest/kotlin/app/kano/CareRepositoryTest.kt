package app.kano

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import app.kano.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class CareRepositoryTest {
    @get:Rule val migrations = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), KanoDatabase::class.java)

    @Test fun upgradePreservesExistingMediaAndStartsCareEmpty() {
        val name = "migration-" + UUID.randomUUID()
        migrations.createDatabase(name, 1).apply {
            execSQL("INSERT INTO media (uri,name,state) VALUES ('content://test/kept','kept.png','INDEXED')")
            close()
        }
        migrations.runMigrationsAndValidate(name, 2, true, KanoDatabase.MIGRATION_1_2).apply {
            query("SELECT name,state FROM media").use { assertTrue(it.moveToFirst()); assertEquals("kept.png", it.getString(0)); assertEquals("INDEXED", it.getString(1)) }
            query("SELECT COUNT(*) FROM care_items").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
            close()
        }
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(name)
    }

    @Test fun explicitInventorySurvivesReopenAndEditsNeverCreateDuplicates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "care-test-" + UUID.randomUUID()
        var db = Room.databaseBuilder(context, KanoDatabase::class.java, name).build()
        try {
            val repo = CareRepository(db)
            assertTrue(repo.items.first().isEmpty())
            repo.save(null, "  Test cleanser  ", "Care", CareStatus.UNKNOWN)
            val id = repo.items.first().single().id
            db.close()
            db = Room.databaseBuilder(context, KanoDatabase::class.java, name).build()
            val reopened = CareRepository(db)
            assertEquals("Test cleanser", reopened.items.first().single().name)
            reopened.save(id, "Updated cleanser", "Skincare", CareStatus.LOW)
            assertEquals("LOW", reopened.items.first().single().status)
            reopened.delete(id)
            reopened.delete(id)
            assertTrue(reopened.items.first().isEmpty())
            try { reopened.save(id, "Missing", "", CareStatus.ACTIVE); fail("Stale edit must not resurrect a deleted record") }
            catch (_: IllegalStateException) { }
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun invalidInputAndInventoryLimitDoNotWritePartialRecords() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KanoDatabase::class.java).build()
        try {
            val repo = CareRepository(db)
            for (invalid in listOf(" ", "a".repeat(121))) {
                try { repo.save(null, invalid, "", CareStatus.UNKNOWN); fail("Invalid name accepted") }
                catch (_: IllegalArgumentException) { }
            }
            assertEquals(0, db.care().count())
            repeat(200) { repo.save(null, "Test item $it", "", CareStatus.UNKNOWN) }
            try { repo.save(null, "Over limit", "", CareStatus.UNKNOWN); fail("Limit ignored") }
            catch (_: IllegalStateException) { }
            assertEquals(200, db.care().count())
            val first = repo.items.first().first()
            repo.save(first.id, first.name, "Edited at capacity", CareStatus.REVIEW)
            assertEquals(200, db.care().count())
        } finally { db.close() }
    }
}

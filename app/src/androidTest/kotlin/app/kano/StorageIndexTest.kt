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

class StorageIndexTest {
    @get:Rule val migrations = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), KanoDatabase::class.java)

    @Test fun migrationPreservesAllPreviousUserTables() {
        val name = "storage-migration-" + UUID.randomUUID()
        migrations.createDatabase(name, 3).apply {
            execSQL("INSERT INTO media(uri,name,state) VALUES('content://fixture/kept','kept.png','INDEXED')")
            execSQL("INSERT INTO care_items(id,name,category,status,updatedAt) VALUES('kept','Kept care','Care','UNKNOWN',1)")
            execSQL("INSERT INTO knowledge_entities(id,entityType,title,detail,sourceUri,extractionType,confidence,createdAt) VALUES('kept','TEXT','Kept memory','Kept detail','content://fixture/kept','OCR','UNCERTAIN',1)")
            close()
        }
        try {
            migrations.runMigrationsAndValidate(name, 4, true, KanoDatabase.MIGRATION_3_4).use { db ->
                listOf("media", "care_items", "knowledge_entities").forEach { table ->
                    db.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); assertEquals(1, it.getInt(0)) }
                }
                db.query("SELECT COUNT(*) FROM storage_entries").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
            }
        } finally { InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(name) }
    }

    @Test fun boundedTenThousandIndexAggregatesAliasesUnknownSizesAndAccess() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KanoDatabase::class.java).build()
        try {
            val dao = db.storage()
            val start = System.nanoTime()
            repeat(50) { batch ->
                dao.saveEntries(List(200) { index ->
                    val id = batch * 200 + index
                    StorageEntry("content://fixture/$id", "fixture:$id", "saf:fixture", name = "fixture-$id.pdf", mime = "application/pdf",
                        sizeBytes = if (id == 0) null else id.toLong(), category = "DOCUMENTS")
                })
            }
            assertEquals(10_000L, dao.count())
            val totals = dao.totals().first()
            assertEquals(10_000L, totals.count)
            assertEquals(49_995_000L, totals.bytes)
            assertEquals(1L, totals.unknownSizes)
            assertEquals(40, dao.page("ALL", 100L*1024*1024, 40, 0).first().size)
            assertEquals(9_999L, dao.page("ALL", 100L*1024*1024, 40, 0).first().first().sizeBytes)
            val original = dao.findIdentity("fixture:1")!!
            dao.saveEntries(listOf(original.copy(uri = "content://fixture/alias")))
            assertEquals(10_000L, dao.count())
            dao.revokeAll()
            assertEquals(0L, dao.count())
            assertTrue(dao.duplicates().first().isEmpty())
            println("STORAGE_ROOM_10000_MS=" + (System.nanoTime()-start)/1_000_000)
        } finally { db.close() }
    }

    @Test fun duplicateSummaryExcludesStaleRevokedAndAliasRows() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KanoDatabase::class.java).build()
        try {
            val dao = db.storage()
            val hash = "a".repeat(64)
            val entries = List(4) { id -> StorageEntry("content://fixture/$id", "fixture:$id", "saf:fixture", name = "$id.txt", sizeBytes = 64,
                hashState = if (id == 2) "STALE" else "VERIFIED", sha256 = hash, accessState = if (id == 3) "REVOKED" else "AUTHORIZED") }
            dao.saveEntries(entries)
            assertEquals(2L, dao.duplicates().first().single().copies)
            dao.classifyDuplicates()
            assertEquals("DUPLICATE", dao.findIdentity("fixture:0")!!.classification)
            assertEquals("UNKNOWN", dao.findIdentity("fixture:2")!!.classification)
            val empty = StorageEntry("content://fixture/empty", "fixture:empty", "saf:fixture", name = "empty.jpg", sizeBytes = 0,
                hashState = "VERIFIED", sha256 = hash)
            dao.saveEntries(listOf(empty, empty.copy(uri="content://fixture/empty2", identity="fixture:empty2")))
            dao.classifyDuplicates()
            assertEquals(1, dao.duplicates().first().size)
            assertEquals("UNKNOWN", dao.findIdentity("fixture:empty")!!.classification)
            assertTrue(dao.candidates(200, -1, "").all { it.sizeBytes > 0 })
            dao.revokeScope("saf:fixture")
            assertTrue(dao.duplicates().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun candidateKeysetsDoNotSkipRowsOrGroupsAfterRevocation() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KanoDatabase::class.java).build()
        try {
            val dao = db.storage()
            dao.saveEntries(List(202) { id -> StorageEntry("content://fixture/$id", "row:${id.toString().padStart(4, '0')}", "saf:fixture",
                name = "$id.bin", sizeBytes = 5, mime = "application/octet-stream") })
            val first = dao.candidatePage(5, "application/octet-stream", true, null, null, null, 100, "")
            dao.saveEntries(first.map { it.copy(accessState = "REVOKED") })
            val next = dao.candidatePage(5, "application/octet-stream", true, null, null, null, 100, first.last().identity)
            assertEquals(100, next.size)
            assertEquals("row:0100", next.first().identity)
            dao.revokeAll()
            dao.saveEntries((1L..401L).flatMap { size -> List(2) { copy -> StorageEntry("content://fixture/$size/$copy", "group:$size:$copy", "saf:fixture",
                name = "$size-$copy.bin", sizeBytes = size, mime = "application/octet-stream") } })
            val groups = dao.candidates(200, -1, "")
            dao.saveEntries(dao.candidatePage(1, "application/octet-stream", true, null, null, null, 2, "").map { it.copy(accessState = "REVOKED") })
            val nextGroups = dao.candidates(200, groups.last().sizeBytes, groups.last().mime.orEmpty())
            assertEquals(201L, nextGroups.first().sizeBytes)
            assertEquals(200, nextGroups.size)
        } finally { db.close() }
    }
}

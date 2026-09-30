package app.kano

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import app.kano.data.*
import app.kano.platform.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/** Isolated metadata fixtures; never scans/mutates the personal phone database or source files. */
class StorageScanTest {
    private class FixtureSources(context: Context) : StorageSources(context) {
        var rows = (1L..1_000L).map { row(it) }
        var signature = "fixture-grant"
        var generation = 1L
        var delayMillis = 0L
        var fail = false
        val cursors = mutableListOf<Long>()
        override fun access() = StorageAccessSnapshot(signature, "Isolated test metadata", true, false)
        override suspend fun scopes() = listOf(StorageScope("ms:fixture", MEDIASTORE, "content://fixture/files", "fixture-version", generation, true))
        override suspend fun page(scope: StorageScope, afterId: Long, sinceGeneration: Long?, limit: Int): StorageBatch {
            if (fail) throw IOException("Fixture read failure")
            delay(delayMillis)
            cursors += afterId
            val remaining = rows.filter { it.sourceId > afterId && (sinceGeneration == null || it.generation!! > sinceGeneration) }
            val batch = remaining.take(limit)
            return StorageBatch(batch, batch.lastOrNull()?.sourceId ?: afterId, remaining.size <= limit)
        }
        override suspend fun visibleIds(scope: StorageScope, afterId: Long, limit: Int): Pair<List<Long>, Boolean> {
            val remaining = rows.filter { it.sourceId > afterId }.map { it.sourceId }
            return remaining.take(limit) to (remaining.size <= limit)
        }
        override suspend fun inspect(uri: String, scope: String) = rows.first { it.uri == uri }
        companion object {
            fun row(id: Long) = StorageEntry("content://fixture/$id", "fixture:$id", "ms:fixture", id, "fixture-$id.bin",
                mime = "application/octet-stream", sizeBytes = id, modifiedAt = 1, generation = 1)
        }
    }

    private suspend fun fixture(test: suspend (KanoDatabase, FixtureSources, StorageRepository) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KanoDatabase::class.java).build()
        val sources = FixtureSources(context)
        val repository = StorageRepository(context, db, sources, WorkManager.getInstance(context))
        try { test(db, sources, repository) } finally { db.close() }
    }
    private suspend fun begin(db: KanoDatabase, sources: FixtureSources, run: Long) {
        db.storage().saveScan(StorageScan(status = "QUEUED", runId = run, scopeSignature = sources.signature, startedAt = System.currentTimeMillis()))
    }

    @Test fun incrementalChangesRemovalsAndUnchangedSourcesAreNotRescanned() = runBlocking {
        fixture { db, source, repo ->
            source.rows = (1L..100L).map { FixtureSources.row(it) }
            begin(db, source, 1); repo.runScan()
            assertEquals(100L, db.storage().count())
            source.cursors.clear()
            begin(db, source, 2); repo.runScan()
            assertTrue(source.cursors.isEmpty())
            assertEquals(0L, db.storage().scan()!!.scanned)
            source.generation = 2
            source.rows = source.rows.drop(1).map { if (it.sourceId == 2L) it.copy(name="changed.bin", generation=2, modifiedAt=2) else it } + FixtureSources.row(101).copy(generation=2)
            begin(db, source, 3); repo.runScan()
            assertEquals(2L, db.storage().scan()!!.scanned)
            assertEquals(1L, db.storage().scan()!!.removed)
            assertEquals(1L, db.storage().scan()!!.added)
            assertEquals(100L, db.storage().count())
            assertEquals("changed.bin", db.storage().findIdentity("fixture:2")!!.name)
            assertEquals("REMOVED", db.storage().findIdentity("fixture:1")!!.accessState)
        }
    }

    @Test fun pauseResumeAndWorkerInterruptionKeepAtomicCheckpoints() = runBlocking {
        fixture { db, source, repo ->
            source.delayMillis = 40
            begin(db, source, 11)
            coroutineScope {
                val running = launch { repo.runScan() }
                withTimeout(10_000) { while ((db.storage().scan()?.scanned ?: 0) == 0L) delay(10) }
                repo.stop(true)
                running.join()
            }
            assertEquals("PAUSED", db.storage().scan()!!.status)
            val saved = db.storage().checkpoint("ms:fixture")!!.cursor
            assertTrue(saved > 0)
            source.cursors.clear()
            db.storage().saveScan(db.storage().scan()!!.copy(status="QUEUED"))
            repo.runScan()
            assertEquals(saved, source.cursors.first())
            assertEquals(1_000L, db.storage().count())
            assertEquals("COMPLETED", db.storage().scan()!!.status)
            begin(db, source, 12)
            source.generation = 2
            source.rows = source.rows.map { it.copy(generation=2,modifiedAt=2) }
            coroutineScope {
                val running = launch { repo.runScan() }
                withTimeout(10_000) { while ((db.storage().scan()?.scanned ?: 0) == 0L) delay(10) }
                running.cancelAndJoin()
            }
            assertEquals("INTERRUPTED", db.storage().scan()!!.status)
            val freshRepo = StorageRepository(ApplicationProvider.getApplicationContext(), db, source, WorkManager.getInstance(ApplicationProvider.getApplicationContext()))
            db.storage().saveScan(db.storage().scan()!!.copy(status="QUEUED"))
            freshRepo.runScan()
            assertEquals("COMPLETED", db.storage().scan()!!.status)
        }
    }

    @Test fun partialFailureNeverMarksUnseenAsRemovedAndRevocationHidesOldRows() = runBlocking {
        fixture { db, source, repo ->
            source.rows = (1L..20L).map { FixtureSources.row(it) }
            begin(db, source, 21); repo.runScan()
            source.generation = 2; source.fail = true
            begin(db, source, 22); repo.runScan()
            assertEquals("PARTIAL_FAILURE", db.storage().scan()!!.status)
            assertEquals(20L, db.storage().count())
            assertEquals(0L, db.storage().scan()!!.removed)
            assertEquals(0L, db.storage().checkpoint("ms:fixture")!!.cursor)
            source.fail = false
            repo.requestScan(resume = true)
            // Run synchronously before the isolated WorkManager request (uses production graph) can start.
            WorkManager.getInstance(ApplicationProvider.getApplicationContext()).cancelUniqueWork(StorageRepository.WORK).result.get()
            repo.runScan()
            assertEquals("COMPLETED", db.storage().scan()!!.status)
            assertEquals(0L, db.storage().scan()!!.errors)
            assertEquals(20L, db.storage().scopePage("ms:fixture", 100, 0).count { it.scanState == "INDEXED" }.toLong())
            source.signature = "revoked-fixture-grant"
            repo.refreshAccess()
            assertEquals("STALE_INDEX", db.storage().scan()!!.status)
            assertEquals(0L, db.storage().count())
            source.fail = false
            source.generation = 1 // Permission changed without a MediaStore generation change.
            begin(db, source, 23); repo.runScan()
            assertEquals(20L, db.storage().count())
            assertEquals("COMPLETED", db.storage().scan()!!.status)
        }
    }
}

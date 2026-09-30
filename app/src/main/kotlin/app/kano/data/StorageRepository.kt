package app.kano.data

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import androidx.core.net.toUri
import androidx.room.withTransaction
import androidx.work.*
import app.kano.core.ContentHasher
import app.kano.core.HashResult
import app.kano.platform.StorageSources
import app.kano.platform.StorageScope
import app.kano.platform.StorageIndexWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/** Metadata index and candidate-only streaming comparison. No original-file mutation APIs. */
class StorageRepository(
    private val context: Context,
    private val db: KanoDatabase,
    val sources: StorageSources,
    private val work: WorkManager,
) {
    private val dao get() = db.storage()
    private val scanner = Mutex()
    private val controls = Mutex()
    private val accessLock = Mutex()
    fun changes() = callbackFlow {
        val observer = object : ContentObserver(null) { override fun onChange(selfChange: Boolean) { trySend(Unit) } }
        context.contentResolver.registerContentObserver(android.provider.MediaStore.Files.getContentUri("external"), true, observer)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.conflate()

    suspend fun refreshAccess() = withContext(Dispatchers.IO) {
      accessLock.withLock {
        val access = sources.access()
        val previous = dao.scan()
        if (previous != null && previous.scopeSignature != access.signature) {
            work.cancelUniqueWork(WORK).result.get()
            db.withTransaction {
                dao.revokeAll()
                dao.invalidateCheckpoints()
                dao.setStatus(if (access.hasSources) "STALE_INDEX" else "NO_ACCESS", "Access changed. A new scan is required; old entries are hidden.")
            }
        } else if (previous != null && access.visualSelection) {
            // Android can change the selected subset without changing permission booleans/generation.
            work.cancelUniqueWork(WORK).result.get()
            dao.revokeAll()
            dao.invalidateCheckpoints()
            if (previous.status !in setOf("PAUSED", "CANCELLED")) dao.setStatus("STALE_INDEX", "Selected-media access must be requeried on return to Kano.")
        } else if (previous?.status in setOf("COMPLETED", "PARTIAL_FAILURE")) {
            val changed = sources.scopes().any { scope ->
                val saved = dao.checkpoint(scope.key)
                scope.kind == "MEDIASTORE" && (saved?.version != scope.version || saved?.generation != scope.generation)
            }
            if (changed) dao.setStatus("STALE_INDEX", "MediaStore changed since the last scan. Incremental update is required.")
        }
        access
      }
    }

    suspend fun requestScan(force: Boolean = false, resume: Boolean = false) = withContext(Dispatchers.IO) {
        controls.withLock {
            val access = refreshAccess()
            val prior = dao.scan()
            if (!access.hasSources) {
                dao.saveScan((prior ?: StorageScan()).copy(status = "NO_ACCESS", scopeSignature = access.signature, detail = "Grant media or select files/folders to scan."))
                return@withLock
            }
            val active = work.getWorkInfosForUniqueWork(WORK).get().any { !it.state.isFinished }
            if (active && prior?.status in setOf("QUEUED", "SCANNING")) return@withLock
            work.cancelUniqueWork(WORK).result.get()
            val canResume = resume && !access.visualSelection && prior != null && prior.scopeSignature == access.signature && prior.status in setOf("PAUSED", "CANCELLED", "INTERRUPTED", "ERROR", "PARTIAL_FAILURE")
            val next = if (canResume) prior!!.copy(status = "QUEUED", errors = 0, finishedAt = null, detail = "Resume queued; unfinished batches and hashes will continue. Errors describe this attempt.")
            else StorageScan(status = "QUEUED", runId = System.currentTimeMillis(), scopeSignature = access.signature, startedAt = System.currentTimeMillis(), detail = "Queued. Android battery restrictions may delay the worker.")
            if (force) sources.scopes().forEach { dao.saveCheckpoint(StorageCheckpoint(it.key)) }
            dao.saveScan(next)
            work.enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<StorageIndexWorker>()
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .build()).result.get()
        }
    }

    suspend fun stop(paused: Boolean) = withContext(Dispatchers.IO) {
        controls.withLock {
            dao.setStatus(if (paused) "PAUSED" else "CANCELLED", if (paused) "Paused; metadata checkpoints and verified hashes retained." else "Cancelled; originals retained. Resume or start a new scan.")
            work.cancelUniqueWork(WORK).result.get()
        }
    }

    suspend fun addGrants(uris: List<Uri>) = withContext(Dispatchers.IO) { sources.addGrants(uris); requestScan() }

    private suspend fun ensureRunning(signature: String) {
        currentCoroutineContext().ensureActive()
        if (dao.scan()?.status != "SCANNING") throw CancellationException("Storage scan stopped")
        if (sources.access().signature != signature) {
            db.withTransaction { dao.revokeAll(); dao.invalidateCheckpoints(); dao.setStatus("STALE_INDEX", "Access changed during scan. Recheck permissions and scan again.") }
            throw CancellationException("Storage access changed")
        }
    }

    private fun sameRevision(a: StorageEntry, b: StorageEntry): Boolean =
        a.sizeBytes == b.sizeBytes && a.mime == b.mime && a.modifiedAt == b.modifiedAt &&
            a.generation == b.generation && a.width == b.width && a.height == b.height &&
            a.durationMillis == b.durationMillis && (b.generation != null || b.modifiedAt != null)

    private suspend fun storeBatch(rows: List<StorageEntry>, scan: StorageScan, checkpoint: StorageCheckpoint?) {
        db.withTransaction {
            val live = dao.scan() ?: return@withTransaction
            if (live.status != "SCANNING" || live.runId != scan.runId) throw CancellationException("Scan stopped")
            var added = 0L
            var updated = 0L
            val merged = rows.mapNotNull { row ->
                val old = dao.findIdentity(row.identity)
                // A picked alias must not count as another file or replace a working MediaStore source.
                if (old?.scope?.startsWith("ms:") == true && row.scope.startsWith("saf:") && old.accessState == "AUTHORIZED") return@mapNotNull null
                if (old == null) added++ else if (!sameRevision(old, row) || old.accessState != "AUTHORIZED") updated++
                val keptHash = old?.takeIf { sameRevision(it, row) && it.accessState == "AUTHORIZED" }
                row.copy(seenRun = scan.runId, indexedAt = System.currentTimeMillis(), sha256 = keptHash?.sha256,
                    hashState = keptHash?.hashState ?: "NOT_NEEDED", classification = keptHash?.classification ?: "UNKNOWN")
            }
            dao.saveEntries(merged)
            checkpoint?.let { dao.saveCheckpoint(it) }
            dao.saveScan(live.copy(scanned = live.scanned + rows.size, added = live.added + added, updated = live.updated + updated, detail = "Reading authorized metadata; no file content is decoded."))
        }
    }

    suspend fun runScan() = withContext(Dispatchers.IO) {
        scanner.withLock {
            var scan = dao.scan() ?: return@withLock
            if (scan.status !in setOf("QUEUED", "SCANNING", "INTERRUPTED", "ERROR")) return@withLock
            if (scan.scopeSignature != sources.access().signature) { refreshAccess(); return@withLock }
            dao.saveScan(scan.copy(status = "SCANNING", detail = "Reading authorized metadata."))
            scan = dao.scan()!!
            try {
                val scopes = sources.scopes()
                dao.revokeOtherScopes(scopes.map { it.key })
                for (scope in scopes) {
                    ensureRunning(scan.scopeSignature)
                    try { indexScope(scope, scan) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: SecurityException) {
                        dao.revokeScope(scope.key)
                        recordError("A source permission was revoked; unavailable entries hidden.")
                    }
                    catch (_: Exception) {
                        dao.staleScope(scope.key)
                        // Replay the complete scope on retry: earlier batches are now stale too.
                        dao.saveCheckpoint(StorageCheckpoint(scope.key))
                        recordError("A source could not be read; its previous metadata remains stale.")
                    }
                }
                ensureRunning(scan.scopeSignature)
                hashCandidates(scan)
                ensureRunning(scan.scopeSignature)
                val changedAfterHashing = sources.scopes().filter { it.kind == StorageSources.MEDIASTORE }.any { scope ->
                    val checkpoint = dao.checkpoint(scope.key)
                    checkpoint?.completed == true && (checkpoint.version != scope.version || checkpoint.generation != scope.generation)
                }
                db.withTransaction {
                    val live = dao.scan()!!
                    dao.classifyDuplicates()
                    if (live.status == "SCANNING") dao.saveScan(live.copy(status = if (changedAfterHashing) "STALE_INDEX" else if (live.errors == 0L) "COMPLETED" else "PARTIAL_FAILURE",
                        finishedAt = System.currentTimeMillis(), detail = if (changedAfterHashing) "MediaStore changed during comparison. Update the index before using duplicate results." else if (live.errors == 0L) "Accessible scope indexed. No originals were changed." else "Some sources or hashes failed. Counts cover readable indexed entries; resume or rescan to retry."))
                }
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    if (dao.scan()?.status == "SCANNING") dao.setStatus("INTERRUPTED", "Worker interrupted. Android can retry; resume retains checkpoints.")
                }
                throw cancelled
            } catch (_: Exception) {
                dao.setStatus("ERROR", "Storage index could not finish. Originals retained; resume or rescan.")
                throw IllegalStateException("STORAGE_INDEX_FAILED")
            }
        }
    }

    private suspend fun indexScope(scope: StorageScope, scan: StorageScan) {
        val old = dao.checkpoint(scope.key)
        val changedSnapshot = scope.kind == "MEDIASTORE" && old != null && (old.version != scope.version || old.targetGeneration != scope.generation)
        if (old?.runId == scan.runId && old.completed && !changedSnapshot) return
        if (old?.runId == scan.runId && changedSnapshot) dao.resetSeen(scope.key)
        var checkpoint = if (old?.runId == scan.runId && !changedSnapshot) old else StorageCheckpoint(scope = scope.key,
            version = scope.version, generation = old?.generation, targetGeneration = scope.generation,
            fullScan = !scope.incremental || old?.version != scope.version || old?.generation == null,
            runId = scan.runId)
        if (scope.kind == "MEDIASTORE") {
            // Delta reads include only new/changed metadata. Visibility is reconciled in ID-only batches.
            if (!checkpoint.fullScan && checkpoint.generation == checkpoint.targetGeneration && old?.completed == true) {
                dao.saveCheckpoint(checkpoint.copy(completed = true)); return
            }
            while (true) {
                ensureRunning(scan.scopeSignature)
                val batch = sources.page(scope, checkpoint.cursor, if (checkpoint.fullScan) null else checkpoint.generation)
                checkpoint = checkpoint.copy(cursor = batch.nextId)
                storeBatch(batch.items, scan, checkpoint)
                if (batch.done) break
            }
            if (!checkpoint.fullScan) {
                var after = 0L
                do {
                    ensureRunning(scan.scopeSignature)
                    val (ids, done) = sources.visibleIds(scope, after)
                    db.withTransaction { ids.forEach { dao.markSeen(scope.key, it, scan.runId) } }
                    if (ids.isNotEmpty()) after = ids.last()
                    if (done) break
                } while (true)
            }
        } else {
            // Provider tree cursor formats vary; replay bounded metadata on resume, reuse unchanged hashes.
            sources.visitDocuments(scope) { batch -> ensureRunning(scan.scopeSignature); storeBatch(batch, scan, null) }
        }
        ensureRunning(scan.scopeSignature)
        val now = sources.scopes().firstOrNull { it.key == scope.key }
        if (now == null || (scope.kind == "MEDIASTORE" && (now.version != scope.version || now.generation != scope.generation))) {
            dao.saveCheckpoint(checkpoint.copy(cursor = 0, completed = false))
            recordError("Media changed during scan. Previous unseen entries were retained; rescan required.")
            return
        }
        db.withTransaction {
            val live = dao.scan()!!
            if (live.status != "SCANNING") throw CancellationException("Scan stopped")
            val removed = dao.markUnseen(scope.key, scan.runId)
            dao.saveCheckpoint(checkpoint.copy(generation = checkpoint.targetGeneration, completed = true))
            dao.saveScan(live.copy(removed = live.removed + removed))
        }
    }

    private suspend fun recordError(detail: String) = db.withTransaction {
        val live = dao.scan() ?: return@withTransaction
        if (live.status == "SCANNING") dao.saveScan(live.copy(errors = live.errors + 1, detail = detail))
    }

    private suspend fun hashCandidates(scan: StorageScan) {
        var afterSize = -1L
        var afterMime = ""
        var candidates = 0L
        while (true) {
            val groups = dao.candidates(200, afterSize, afterMime)
            if (groups.isEmpty()) break
            for (group in groups.flatMap { hashBuckets(it) }) {
                var afterIdentity = ""
                while (true) {
                    val rows = candidatePage(group, afterIdentity)
                    if (rows.isEmpty()) break
                    candidates += rows.size
                    afterIdentity = rows.last().identity
                }
            }
            afterSize = groups.last().sizeBytes
            afterMime = groups.last().mime.orEmpty()
        }
        db.withTransaction { val live = dao.scan()!!; if (live.status == "SCANNING") dao.saveScan(live.copy(phase = "HASHING", hashed = 0, hashCandidates = candidates, detail = "Comparing candidate accessible streams with SHA-256.")) }
        afterSize = -1L
        afterMime = ""
        while (true) {
            ensureRunning(scan.scopeSignature)
            val groups = dao.candidates(200, afterSize, afterMime)
            if (groups.isEmpty()) break
            for (group in groups.flatMap { hashBuckets(it) }) {
                var afterIdentity = ""
                while (true) {
                    ensureRunning(scan.scopeSignature)
                    val rows = candidatePage(group, afterIdentity)
                    if (rows.isEmpty()) break
                    for (pair in rows.chunked(2)) {
                        val results = coroutineScope { pair.map { async(Dispatchers.IO) { hashOne(it, scan.scopeSignature) } }.awaitAll() }
                        db.withTransaction {
                            val live = dao.scan()!!
                            if (live.status != "SCANNING") throw CancellationException("Scan stopped")
                            dao.saveEntries(results)
                            dao.saveScan(live.copy(hashed = live.hashed + results.size, errors = live.errors + results.count { it.hashState in setOf("ERROR", "SOURCE_CHANGED", "REVOKED") }))
                        }
                    }
                    afterIdentity = rows.last().identity
                }
            }
            afterSize = groups.last().sizeBytes
            afterMime = groups.last().mime.orEmpty()
        }
    }

    private suspend fun hashOne(row: StorageEntry, signature: String): StorageEntry {
        ensureRunning(signature)
        if (row.hashState == "VERIFIED" && row.sha256 != null) return row
        if (row.generation == null && row.modifiedAt == null) return row.copy(sha256 = null, hashState = "UNAVAILABLE", classification = "UNKNOWN")
        return try {
            val before = sources.inspect(row.uri, row.scope)
            if (!sameRevision(row, before)) return row.copy(sha256 = null, hashState = "SOURCE_CHANGED", classification = "UNKNOWN")
            val job = currentCoroutineContext()
            val result = context.contentResolver.openInputStream(row.uri.toUri())?.use { stream ->
                ContentHasher.hash(stream, row.sizeBytes!!, Long.MAX_VALUE) { job.ensureActive() }
            } ?: return row.copy(hashState = "ERROR", sha256 = null)
            ensureRunning(signature)
            val after = sources.inspect(row.uri, row.scope)
            if (!sameRevision(before, after)) return row.copy(hashState = "SOURCE_CHANGED", sha256 = null, classification = "UNKNOWN")
            when (result) {
                is HashResult.Complete -> row.copy(sha256 = result.sha256, hashState = "VERIFIED", classification = "UNKNOWN")
                else -> row.copy(sha256 = null, hashState = "SOURCE_CHANGED", classification = "UNKNOWN")
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: SecurityException) { row.copy(sha256 = null, hashState = "REVOKED", accessState = "REVOKED") }
        catch (_: Exception) { row.copy(sha256 = null, hashState = "ERROR") }
    }

    private data class HashBucket(val size: Long, val mime: String?, val broad: Boolean = true, val width: Int? = null, val height: Int? = null, val duration: Long? = null)
    private suspend fun hashBuckets(candidate: StorageCandidate): List<HashBucket> {
        val shapes = dao.shapes(candidate.sizeBytes, candidate.mime)
        val video = candidate.mime?.startsWith("video/") == true
        val image = candidate.mime?.startsWith("image/") == true
        if ((!video && !image) || shapes.any { it.width == null || it.height == null || (video && it.durationMillis == null) }) {
            return listOf(HashBucket(candidate.sizeBytes, candidate.mime))
        }
        return shapes.filter { it.copies > 1 }.map { HashBucket(candidate.sizeBytes, candidate.mime, false, it.width, it.height, if (video) it.durationMillis else null) }
    }
    private suspend fun candidatePage(bucket: HashBucket, afterIdentity: String) = dao.candidatePage(bucket.size, bucket.mime, bucket.broad, bucket.width, bucket.height, bucket.duration, 100, afterIdentity)

    /** Only generated code cache is known reconstructible; private photos/drafts are excluded. */
    suspend fun temporaryBytes(): Long? = withContext(Dispatchers.IO) {
        try {
            var bytes = 0L
            context.codeCacheDir.walkTopDown().forEach { currentCoroutineContext().ensureActive(); if (it.isFile) bytes = Math.addExact(bytes, it.length()) }
            bytes
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null }
    }

    companion object { const val WORK = "kano-storage-intelligence" }
}

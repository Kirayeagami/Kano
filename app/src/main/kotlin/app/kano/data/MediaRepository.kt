package app.kano.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.net.toUri
import java.io.IOException
import app.kano.core.ContentHasher
import app.kano.core.HashResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class ImportSummary(val accepted: Int, val rejected: Int)

class MediaRepository(context: Context, private val dao: MediaDao) {
    private val resolver = context.contentResolver
    private val mutation = Mutex()

    suspend fun import(uris: List<Uri>): ImportSummary = withContext(Dispatchers.IO) {
        mutation.withLock {
            var accepted = 0
            var rejected = 0
            val existing = dao.all().map { it.uri }.toMutableSet()
            for (uri in uris.distinct()) {
                currentCoroutineContext().ensureActive()
                if (uri.scheme != "content" || (uri.toString() !in existing && existing.size >= MAX_ITEMS)) {
                    rejected++
                    continue
                }
                try {
                    val hadAccess = resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        dao.add(MediaRecord(uri = uri.toString()))
                    } catch (failure: Exception) {
                        // Do not leave a newly acquired grant behind if persistence fails.
                        if (!hadAccess) {
                            try { resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            catch (_: SecurityException) { /* Already revoked. */ }
                        }
                        throw failure
                    }
                    existing.add(uri.toString())
                    accepted++
                } catch (_: SecurityException) {
                    rejected++
                }
            }
            ImportSummary(accepted, rejected)
        }
    }

    suspend fun scan() = withContext(Dispatchers.IO) {
        mutation.withLock {
            for (record in dao.all()) {
                currentCoroutineContext().ensureActive()
                val uri = record.uri.toUri()
                if (resolver.persistedUriPermissions.none { it.uri == uri && it.isReadPermission }) {
                    dao.save(MediaRecord(record.uri, state = "ACCESS_REVOKED", errorCode = "SELECT_AGAIN"))
                    continue
                }
                dao.save(record.copy(state = "INDEXING", sha256 = null, errorCode = null))
                val result = try {
                    inspect(uri)
                } catch (_: SecurityException) {
                    MediaRecord(record.uri, state = "ACCESS_REVOKED", errorCode = "SELECT_AGAIN")
                } catch (_: IOException) {
                    MediaRecord(record.uri, state = "ERROR", errorCode = "SOURCE_UNAVAILABLE")
                } catch (_: IllegalArgumentException) {
                    MediaRecord(record.uri, state = "ERROR", errorCode = "UNSUPPORTED_PROVIDER")
                }
                currentCoroutineContext().ensureActive()
                dao.save(result)
            }
        }
    }

    private suspend fun inspect(uri: Uri): MediaRecord {
        var name = "Selected document"
        var size: Long? = null
        var modified: Long? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) throw IOException("No document metadata")
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn >= 0 && !cursor.isNull(nameColumn)) name = cursor.getString(nameColumn).take(240)
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) size = cursor.getLong(sizeColumn).takeIf { it >= 0 }
            val modifiedColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            if (modifiedColumn >= 0 && !cursor.isNull(modifiedColumn)) modified = cursor.getLong(modifiedColumn).takeIf { it > 0 }
        } ?: throw IOException("No document cursor")
        val mime = resolver.getType(uri)
        val base = MediaRecord(uri.toString(), name, mime, size, modified, indexedAt = System.currentTimeMillis())
        if (mime == null || (!mime.startsWith("image/") && !mime.startsWith("video/"))) {
            return base.copy(state = "UNSUPPORTED", errorCode = "NOT_IMAGE_OR_VIDEO")
        }
        val length = size
        if (length == null || length > MAX_HASH_BYTES) return base.copy(state = "METADATA_ONLY", errorCode = "HASH_SIZE_LIMIT")
        val coroutineContext = currentCoroutineContext()
        val hash = resolver.openInputStream(uri)?.use { stream ->
            ContentHasher.hash(stream, length, MAX_HASH_BYTES) { coroutineContext.ensureActive() }
        } ?: throw IOException("No document stream")
        return when (hash) {
            is HashResult.Complete -> base.copy(state = "INDEXED", sha256 = hash.sha256)
            HashResult.LimitExceeded -> base.copy(state = "METADATA_ONLY", errorCode = "HASH_SIZE_LIMIT")
            HashResult.SourceChanged -> base.copy(state = "ERROR", errorCode = "SOURCE_CHANGED")
        }
    }

    /** Cancellation precedes this call; mutex prevents a late worker write after clear. */
    suspend fun forget(): Int = withContext(Dispatchers.IO) {
        mutation.withLock {
            var unreleased = 0
            for (record in dao.all()) {
                try {
                    resolver.releasePersistableUriPermission(record.uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Already revoked: there is no grant left to release.
                } catch (_: IllegalArgumentException) {
                    unreleased++
                }
            }
            dao.clear()
            unreleased
        }
    }

    companion object {
        const val MAX_ITEMS = 100
        const val MAX_HASH_BYTES = 100L * 1024 * 1024
        fun searchPattern(query: String): String = "%" + query.replace("\\", "\\\\")
            .replace("%", "\\%").replace("_", "\\_") + "%"
    }
}

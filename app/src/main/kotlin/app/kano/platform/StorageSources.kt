package app.kano.platform

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import app.kano.core.PrivacyFirewall
import app.kano.core.StorageRules
import app.kano.data.StorageEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.ArrayDeque
import kotlin.coroutines.resumeWithException

data class StorageAccessSnapshot(val signature: String, val label: String, val hasSources: Boolean, val limited: Boolean, val visualSelection: Boolean = false)
data class StorageScope(val key: String, val kind: String, val uri: String, val version: String?, val generation: Long?, val incremental: Boolean)
data class StorageBatch(val items: List<StorageEntry>, val nextId: Long, val done: Boolean)

/** Read-only public-provider metadata. It never deletes sources or requests write/all-files access. */
open class StorageSources(context: Context) {
    private val context = context.applicationContext
    private val resolver = this.context.contentResolver

    private fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    private fun images() = if (Build.VERSION.SDK_INT >= 33) granted(Manifest.permission.READ_MEDIA_IMAGES) else granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun videos() = if (Build.VERSION.SDK_INT >= 33) granted(Manifest.permission.READ_MEDIA_VIDEO) else granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun audio() = if (Build.VERSION.SDK_INT >= 33) granted(Manifest.permission.READ_MEDIA_AUDIO) else granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun selected() = Build.VERSION.SDK_INT >= 34 && granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    private fun readGrants() = resolver.persistedUriPermissions.filter { it.isReadPermission && it.uri.authority != "${context.packageName}.files" }.map { it.uri }.distinct().sortedBy(Uri::toString)

    open fun access(): StorageAccessSnapshot {
        val image = images(); val video = videos(); val sound = audio(); val partial = selected()
        val grants = readGrants()
        val labels = mutableListOf<String>()
        if (image && video) labels += "Photos and videos granted"
        else {
            if (image) labels += "Photos granted"
            if (video) labels += "Videos granted"
            if (partial) labels += "Selected photos/videos only"
        }
        if (sound) labels += "Audio granted"
        if (!image && !video && !sound && !partial) labels += if (Build.VERSION.SDK_INT >= 29)
            "Own media/downloads only · other media access not granted" else "Media access not granted"
        if (grants.isNotEmpty()) labels += "${grants.size} selected document/folder grants"
        val signature = PrivacyFirewall.digest("${Build.VERSION.SDK_INT}:$image:$video:$sound:$partial:" + grants.joinToString("\n"))
        return StorageAccessSnapshot(signature, labels.joinToString(" · "), image || video || sound || partial || grants.isNotEmpty() || Build.VERSION.SDK_INT >= 29,
            !(image && video && sound), visualSelection = partial && !(image && video))
    }

    fun permissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED, Manifest.permission.READ_MEDIA_AUDIO)
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    open suspend fun scopes(): List<StorageScope> = withContext(Dispatchers.IO) {
        val result = mutableListOf<StorageScope>()
        if (Build.VERSION.SDK_INT >= 29 || images() || videos() || audio()) {
            val volumes = if (Build.VERSION.SDK_INT >= 29) MediaStore.getExternalVolumeNames(context).sorted() else listOf("external")
            for (volume in volumes) {
                currentCoroutineContext().ensureActive()
                val version = runCatching { if (Build.VERSION.SDK_INT >= 29) MediaStore.getVersion(context, volume) else MediaStore.getVersion(context) }.getOrNull()
                val generation = if (Build.VERSION.SDK_INT >= 30) runCatching { MediaStore.getGeneration(context, volume) }.getOrNull() else null
                result += StorageScope("ms:$volume", MEDIASTORE, MediaStore.Files.getContentUri(volume).toString(), version, generation,
                    images() && videos() && version != null && generation != null)
            }
        }
        for (uri in readGrants()) result += StorageScope("saf:$uri", if (DocumentsContract.isTreeUri(uri)) SAF_TREE else SAF_DOCUMENT, uri.toString(), null, null, false)
        result
    }

    /** Keyset paging remains bounded even if a provider ignores QUERY_ARG_LIMIT. */
    open suspend fun page(scope: StorageScope, afterId: Long, sinceGeneration: Long?, limit: Int = 200): StorageBatch = withContext(Dispatchers.IO) {
        require(scope.kind == MEDIASTORE && afterId >= 0 && limit in 1..MAX_BATCH)
        val selections = mediaSelections(afterId, if (scope.incremental) sinceGeneration else null, if (scope.incremental) scope.generation else null)
        val readContext = currentCoroutineContext()
        val combined = ArrayList<StorageEntry>((limit + 1) * selections.size)
        for ((selection, arguments) in selections) {
            readContext.ensureActive()
            combined += cancellableProviderRead { signal ->
                resolver.query(Uri.parse(scope.uri), mediaProjection(), queryArgs(selection, arguments, limit + 1), signal)?.use { cursor ->
                    val items = ArrayList<StorageEntry>(limit + 1)
                    var last = afterId
                    while (items.size <= limit && cursor.moveToNext()) {
                        readContext.ensureActive()
                        val id = cursor.number(MediaStore.MediaColumns._ID) ?: throw IOException("Media identifier unavailable")
                        check(id > last) { "Media provider did not honor keyset ordering" }
                        items += mediaEntry(cursor, scope.key, Uri.parse(scope.uri), id)
                        last = id
                    }
                    items
                } ?: throw IOException("Media provider unavailable")
            }
        }
        val merged = combined.distinctBy { it.sourceId }.sortedBy { it.sourceId }
        val items = merged.take(limit)
        StorageBatch(items, items.lastOrNull()?.sourceId ?: afterId, merged.size <= limit)
    }

    /** Full visibility, not generation delta: removed/unselected rows have no delta tombstone. */
    open suspend fun visibleIds(scope: StorageScope, afterId: Long, limit: Int = 200): Pair<List<Long>, Boolean> = withContext(Dispatchers.IO) {
        require(scope.kind == MEDIASTORE && afterId >= 0 && limit in 1..MAX_BATCH)
        val selections = mediaSelections(afterId, null, null)
        val readContext = currentCoroutineContext()
        val combined = ArrayList<Long>((limit + 1) * selections.size)
        for ((selection, arguments) in selections) {
            readContext.ensureActive()
            combined += cancellableProviderRead { signal ->
                resolver.query(Uri.parse(scope.uri), arrayOf(MediaStore.MediaColumns._ID), queryArgs(selection, arguments, limit + 1), signal)?.use { cursor ->
                    val ids = ArrayList<Long>(limit + 1)
                    var last = afterId
                    while (ids.size <= limit && cursor.moveToNext()) {
                        readContext.ensureActive()
                        val id = cursor.number(MediaStore.MediaColumns._ID) ?: throw IOException("Media identifier unavailable")
                        check(id > last) { "Media provider did not honor keyset ordering" }
                        ids += id; last = id
                    }
                    ids
                } ?: throw IOException("Media provider unavailable")
            }
        }
        val merged = combined.distinct().sorted()
        merged.take(limit) to (merged.size <= limit)
    }

    open suspend fun inspect(uri: String, scope: String): StorageEntry = withContext(Dispatchers.IO) {
        val source = Uri.parse(uri)
        require(source.scheme == "content")
        currentCoroutineContext().ensureActive()
        if (scope.startsWith("saf:")) {
            ensureGrant(Uri.parse(scope.removePrefix("saf:")))
            inspectDocument(source, scope)
        } else {
            require(scope.startsWith("ms:") && source.authority == MediaStore.AUTHORITY)
            inspectMedia(source, scope)
        }
    }

    /** Bounded cursors and at most 4096 pending/visited directories, not a full file list. */
    open suspend fun visitDocuments(scope: StorageScope, emit: suspend (List<StorageEntry>) -> Unit): Unit = withContext(Dispatchers.IO) {
        require(scope.kind == SAF_DOCUMENT || scope.kind == SAF_TREE)
        val grant = Uri.parse(scope.uri)
        ensureGrant(grant)
        if (scope.kind == SAF_DOCUMENT) {
            val entry = inspectDocument(grant, scope.key)
            if (entry.mime != DocumentsContract.Document.MIME_TYPE_DIR) emit(listOf(entry))
            ensureGrant(grant)
            return@withContext
        }
        val pending = ArrayDeque<String>()
        val visited = HashSet<String>()
        pending.add(DocumentsContract.getTreeDocumentId(grant))
        while (pending.isNotEmpty()) {
            currentCoroutineContext().ensureActive(); ensureGrant(grant)
            val directory = pending.removeFirst()
            if (!visited.add(directory)) continue
            if (visited.size > MAX_DIRECTORIES) throw IOException("Folder traversal limit reached; scan is incomplete")
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(grant, directory)
            openDocumentCursor(children, DOCUMENT_PROJECTION).use { cursor ->
                val batch = ArrayList<StorageEntry>(DOCUMENT_BATCH)
                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    val id = cursor.text(DocumentsContract.Document.COLUMN_DOCUMENT_ID) ?: throw IOException("Document identifier unavailable")
                    val mime = cursor.text(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        if (id !in visited) {
                            if (pending.size >= MAX_DIRECTORIES) throw IOException("Folder traversal limit reached; scan is incomplete")
                            pending.add(id)
                        }
                    } else {
                        val child = DocumentsContract.buildDocumentUriUsingTree(grant, id)
                        batch += documentEntry(child, scope.key, cursor)
                        if (batch.size == DOCUMENT_BATCH) {
                            ensureGrant(grant); emit(batch.toList()); batch.clear()
                        }
                    }
                }
                if (batch.isNotEmpty()) { ensureGrant(grant); emit(batch.toList()) }
            }
        }
        ensureGrant(grant)
    }

    /** Persist only an explicit picker read grant. No write permission or provider scanning here. */
    fun addGrants(uris: List<Uri>) {
        for (uri in uris.distinct()) {
            require(uri.scheme == "content" && uri.authority != "${context.packageName}.files")
            if (readGrants().none { it == uri }) resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun ensureGrant(uri: Uri) {
        if (readGrants().none { it == uri }) throw SecurityException("Document read access was revoked")
    }

    private fun mediaSelections(afterId: Long, since: Long?, upper: Long?): List<Pair<String, Array<String>>> {
        val types = mutableListOf<Int>()
        if (images() || selected()) types += MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        if (videos() || selected()) types += MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        if (audio()) types += MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
        val clauses = mutableListOf("_id>?")
        val arguments = mutableListOf(afterId.toString())
        if (Build.VERSION.SDK_INT >= 29) clauses += "is_pending=0"
        if (Build.VERSION.SDK_INT >= 30) {
            clauses += "is_trashed=0"
            if (since != null) { clauses += "generation_modified>?"; arguments += since.toString() }
            if (upper != null) { clauses += "generation_modified<=?"; arguments += upper.toString() }
        }
        val tail = clauses.joinToString(" AND ")
        val result = mutableListOf<Pair<String, Array<String>>>()
        // Some OEM providers suppress granted media when an owner clause appears in that query.
        // Keep granted types and explicitly owned items independent, then merge bounded pages.
        if (types.isNotEmpty()) result += "media_type IN (${types.joinToString(",") { "?" }}) AND $tail" to
            (types.map(Int::toString) + arguments).toTypedArray()
        if (Build.VERSION.SDK_INT >= 29) result += "owner_package_name=? AND (media_type IN (1,2,3) OR relative_path LIKE 'Download/%') AND $tail" to
            (listOf(context.packageName) + arguments).toTypedArray()
        return result
    }

    private fun queryArgs(selection: String, arguments: Array<String>, limit: Int) = Bundle().apply {
        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
        putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arguments)
        putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "_id ASC")
        putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
    }

    private fun mediaProjection(): Array<String> = (mutableListOf("_id", "_display_name", "mime_type", "_size", "date_added", "date_modified", "width", "height", "duration", "media_type").apply {
        if (Build.VERSION.SDK_INT >= 29) addAll(listOf("relative_path", "volume_name"))
        if (Build.VERSION.SDK_INT >= 30) add("generation_modified")
    }).toTypedArray()

    private fun mediaEntry(cursor: Cursor, scope: String, collection: Uri, id: Long): StorageEntry {
        val volume = cursor.text("volume_name") ?: scope.removePrefix("ms:")
        val mime = cursor.text("mime_type")
        val type = cursor.number("media_type")
        val source = if (Build.VERSION.SDK_INT >= 29) when (type) {
            1L -> MediaStore.Images.Media.getContentUri(volume)
            2L -> MediaStore.Audio.Media.getContentUri(volume)
            3L -> MediaStore.Video.Media.getContentUri(volume)
            else -> MediaStore.Files.getContentUri(volume)
        } else when (type) {
            1L -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            2L -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            3L -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> collection
        }
        val name = cursor.text("_display_name")?.take(240) ?: "Unnamed media"
        val location = cursor.text("relative_path")?.take(1000)
        return StorageEntry(uri = ContentUris.withAppendedId(source, id).toString(), identity = "media:$volume:$id", scope = scope, sourceId = id,
            name = name, mime = mime, sizeBytes = cursor.number("_size")?.takeIf { it >= 0 },
            addedAt = milliseconds(cursor.number("date_added")), modifiedAt = milliseconds(cursor.number("date_modified")),
            width = positiveInt(cursor.number("width")), height = positiveInt(cursor.number("height")), durationMillis = cursor.number("duration")?.takeIf { it > 0 },
            location = location, category = StorageRules.category(name, mime, location), generation = cursor.number("generation_modified"), indexedAt = System.currentTimeMillis())
    }

    private suspend fun inspectMedia(uri: Uri, scope: String): StorageEntry = cancellableProviderRead { signal ->
        // Typed collections reject FileColumns such as media_type. Their file IDs share Files.
        val collection = MediaStore.Files.getContentUri(scope.removePrefix("ms:"))
        val metadataUri = ContentUris.withAppendedId(collection, ContentUris.parseId(uri))
        resolver.query(metadataUri, mediaProjection(), null, null, null, signal)?.use { cursor ->
            if (!cursor.moveToFirst()) throw IOException("Media source is no longer visible")
            val id = cursor.number("_id") ?: throw IOException("Media identifier unavailable")
            mediaEntry(cursor, scope, collection, id).copy(uri = uri.toString())
        } ?: throw IOException("Media source unavailable")
    }

    private suspend fun inspectDocument(uri: Uri, scope: String): StorageEntry {
        val projection = if (DocumentsContract.isDocumentUri(context, uri)) DOCUMENT_PROJECTION else arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        openDocumentCursor(uri, projection).use { cursor ->
            if (!cursor.moveToFirst()) throw IOException("Document source unavailable")
            return documentEntry(uri, scope, cursor)
        }
    }

    private suspend fun documentEntry(uri: Uri, scope: String, cursor: Cursor): StorageEntry {
        val documentId = cursor.text(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val name = cursor.text(OpenableColumns.DISPLAY_NAME)?.take(240) ?: "Unnamed document"
        val mime = cursor.text(DocumentsContract.Document.COLUMN_MIME_TYPE) ?: resolver.getType(uri)
        val location = documentId?.take(1000)
        val flags = cursor.number(DocumentsContract.Document.COLUMN_FLAGS) ?: 0
        val entry = StorageEntry(uri = uri.toString(), identity = "document:" + PrivacyFirewall.digest("${uri.authority}:${documentId ?: uri}"), scope = scope,
            name = name, mime = mime, sizeBytes = cursor.number(OpenableColumns.SIZE)?.takeIf { it >= 0 },
            modifiedAt = cursor.number(DocumentsContract.Document.COLUMN_LAST_MODIFIED)?.takeIf { it > 0 }, location = location,
            category = StorageRules.category(name, mime, location), hashState = if ((flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT.toLong()) != 0L) "UNAVAILABLE" else "NOT_NEEDED", indexedAt = System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= 29 && uri.authority in setOf("com.android.providers.media.documents", "com.android.externalstorage.documents")) {
            val media = try { MediaStore.getMediaUri(context, uri) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
            if (media != null) {
                try {
                    val canonical = inspectMedia(media, "ms:${MediaStore.getVolumeName(media)}")
                    return entry.copy(identity = canonical.identity, sourceId = canonical.sourceId, generation = canonical.generation,
                        width = canonical.width, height = canonical.height, durationMillis = canonical.durationMillis,
                        modifiedAt = canonical.modifiedAt ?: entry.modifiedAt, addedAt = canonical.addedAt, location = canonical.location ?: entry.location,
                        category = StorageRules.category(entry.name, entry.mime, canonical.location ?: entry.location))
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* SAF metadata remains valid; canonical provider is unavailable. */ }
            }
        }
        return entry
    }

    /** Close a cursor if cancellation races with delivery; consumers close it with use. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun openDocumentCursor(uri: Uri, projection: Array<String>): Cursor = suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        try {
            val cursor = resolver.query(uri, projection, null, null, null, signal) ?: throw IOException("Document provider unavailable")
            continuation.resume(cursor) { cursor.close() }
        } catch (failure: Exception) { if (continuation.isActive) continuation.resumeWithException(failure) }
    }

    private fun Cursor.number(column: String): Long? = getColumnIndex(column).takeIf { it >= 0 }?.let { if (isNull(it)) null else getLong(it) }
    private fun Cursor.text(column: String): String? = getColumnIndex(column).takeIf { it >= 0 }?.let { if (isNull(it)) null else getString(it) }
    private fun milliseconds(seconds: Long?): Long? = seconds?.takeIf { it > 0 && it <= Long.MAX_VALUE / 1000 }?.times(1000)
    private fun positiveInt(value: Long?): Int? = value?.takeIf { it in 1..Int.MAX_VALUE.toLong() }?.toInt()

    companion object {
        const val MEDIASTORE = "MEDIASTORE"
        const val SAF_DOCUMENT = "SAF_DOCUMENT"
        const val SAF_TREE = "SAF_TREE"
        private const val MAX_BATCH = 500
        private const val DOCUMENT_BATCH = 200
        private const val MAX_DIRECTORIES = 4096
        private val DOCUMENT_PROJECTION = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE, DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_FLAGS)
    }
}

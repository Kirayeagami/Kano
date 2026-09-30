package app.kano.platform

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import app.kano.data.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

enum class GalleryAccess(val label: String) {
    FULL("Full photos and videos"), IMAGES_ONLY("Photos only"), VIDEOS_ONLY("Videos only"),
    PARTIAL("Selected photos and videos"), DENIED("Access not granted"), REVOKED("Access revoked"),
}
enum class GalleryFilter(val label: String) {
    ALL("All"), PHOTOS("Photos"), VIDEOS("Videos"), SCREENSHOTS("Likely screenshots"), LARGE("Large files"),
}
data class GalleryItem(
    val uri: Uri, val name: String, val mime: String, val size: Long?,
    val dateAddedSeconds: Long, val takenAt: Long?, val width: Int?, val height: Int?,
    val durationMillis: Long?, val album: String?, val favorite: Boolean?,
) { val isVideo: Boolean get() = mime.startsWith("video/") }
data class GalleryPage(val items: List<GalleryItem>, val hasMore: Boolean, val capturedAt: Long)

/** MediaStore is queried in bounded pages; inaccessible assets are never treated as an empty full library. */
class GalleryRepository(private val context: Context) {
    private val resolver = context.contentResolver
    fun access(): GalleryAccess {
        fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT < 33) return if (granted(Manifest.permission.READ_EXTERNAL_STORAGE)) GalleryAccess.FULL else GalleryAccess.DENIED
        val images = granted(Manifest.permission.READ_MEDIA_IMAGES)
        val videos = granted(Manifest.permission.READ_MEDIA_VIDEO)
        return when {
            images && videos -> GalleryAccess.FULL
            images -> GalleryAccess.IMAGES_ONLY
            videos -> GalleryAccess.VIDEOS_ONLY
            Build.VERSION.SDK_INT >= 34 && granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> GalleryAccess.PARTIAL
            else -> GalleryAccess.DENIED
        }
    }
    fun permissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    suspend fun page(offset: Int, filter: GalleryFilter, search: String): GalleryPage = withContext(Dispatchers.IO) {
        require(offset >= 0)
        val permission = access()
        if (permission in setOf(GalleryAccess.DENIED, GalleryAccess.REVOKED)) throw SecurityException()
        val columns = mutableListOf("_id", "_display_name", "mime_type", "_size", "date_added", "datetaken", "width", "height", "duration", "bucket_display_name", "media_type")
        if (Build.VERSION.SDK_INT >= 30) columns += "is_favorite"
        val clauses = mutableListOf<String>()
        val arguments = mutableListOf<String>()
        val permittedTypes = when (permission) { GalleryAccess.IMAGES_ONLY -> setOf(1); GalleryAccess.VIDEOS_ONLY -> setOf(3); else -> setOf(1, 3) }
        val requestedTypes = when (filter) { GalleryFilter.PHOTOS, GalleryFilter.SCREENSHOTS -> setOf(1); GalleryFilter.VIDEOS -> setOf(3); else -> setOf(1, 3) }
        val types = requestedTypes.intersect(permittedTypes)
        if (types.isEmpty()) return@withContext GalleryPage(emptyList(), false, System.currentTimeMillis())
        clauses += "media_type IN (${types.joinToString(",") { "?" }})"; arguments += types.map(Int::toString)
        if (search.isNotBlank()) { clauses += "_display_name LIKE ? ESCAPE '\\'"; arguments += MediaRepository.searchPattern(search.take(160)) }
        if (filter == GalleryFilter.SCREENSHOTS) { clauses += "bucket_display_name LIKE ?"; arguments += "%screenshot%" }
        if (filter == GalleryFilter.LARGE) { clauses += "_size >= ?"; arguments += (100L * 1024 * 1024).toString() }
        val readContext = currentCoroutineContext()
        cancellableProviderRead { signal ->
            val bundle = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, clauses.joinToString(" AND "))
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arguments.toTypedArray())
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "date_added DESC, _id DESC")
                putInt(ContentResolver.QUERY_ARG_LIMIT, PAGE_SIZE + 1)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            }
            val collection = MediaStore.Files.getContentUri("external")
            val items = mutableListOf<GalleryItem>()
            var queried = resolver.query(collection, columns.toTypedArray(), bundle, signal)
            if (offset > 0 && queried != null && queried.extras.getStringArray(ContentResolver.EXTRA_HONORED_ARGS)?.contains(ContentResolver.QUERY_ARG_OFFSET) != true) {
                queried.close()
                queried = resolver.query(collection, columns.toTypedArray(), clauses.joinToString(" AND "), arguments.toTypedArray(), "date_added DESC, _id DESC", signal)
                if (queried != null && !queried.moveToPosition(offset - 1)) {
                    queried.close()
                    return@cancellableProviderRead GalleryPage(emptyList(), false, System.currentTimeMillis())
                }
            }
            queried?.use { cursor ->
                fun long(name: String): Long? = cursor.getColumnIndex(name).takeIf { it >= 0 }?.let { if (cursor.isNull(it)) null else cursor.getLong(it) }
                fun string(name: String): String? = cursor.getColumnIndex(name).takeIf { it >= 0 }?.let { if (cursor.isNull(it)) null else cursor.getString(it) }
                while (items.size <= PAGE_SIZE && cursor.moveToNext()) {
                    readContext.ensureActive()
                    val video = long("media_type") == 3L
                    val source = if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    items += GalleryItem(ContentUris.withAppendedId(source, long("_id") ?: continue),
                        string("_display_name") ?: "Unnamed media", string("mime_type") ?: if (video) "video/*" else "image/*",
                        long("_size")?.takeIf { it >= 0 }, long("date_added") ?: 0,
                        long("datetaken")?.takeIf { it > 0 }, long("width")?.toInt()?.takeIf { it > 0 },
                        long("height")?.toInt()?.takeIf { it > 0 }, long("duration")?.takeIf { it > 0 },
                        string("bucket_display_name"), long("is_favorite")?.let { it == 1L })
                }
            } ?: error("Media provider unavailable")
            GalleryPage(items.take(PAGE_SIZE), items.size > PAGE_SIZE, System.currentTimeMillis())
        }
    }
    companion object { const val PAGE_SIZE = 60 }
}

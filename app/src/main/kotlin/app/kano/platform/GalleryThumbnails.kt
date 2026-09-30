package app.kano.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

/** A byte-bounded thumbnail cache. Original images never enter this cache. */
object GalleryThumbnails {
    private val cache = object : LruCache<String, Bitmap>(8 * 1024 * 1024) { override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount }
    private val decoders = Semaphore(3)
    private val generation = AtomicLong()
    fun cachedBytes(): Long = synchronized(cache) { cache.size().toLong() }
    fun clear() { synchronized(cache) { generation.incrementAndGet(); cache.evictAll() } }
    suspend fun load(context: Context, uri: Uri, video: Boolean): Bitmap? = withContext(Dispatchers.IO) {
        val requestedGeneration = generation.get()
        cache.get(uri.toString()) ?: decoders.withPermit {
            try {
                currentCoroutineContext().ensureActive()
                val bitmap = if (Build.VERSION.SDK_INT >= 29 && uri.authority != "${context.packageName}.files") cancellableProviderRead { signal -> context.contentResolver.loadThumbnail(uri, Size(320, 320), signal) }
                else if (!video) {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                    require(options.outWidth > 0 && options.outHeight > 0)
                    var sample = 1
                    while (options.outWidth / sample > 320 || options.outHeight / sample > 320) sample *= 2
                    val decoded = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
                    val exif = context.contentResolver.openInputStream(uri)?.use { androidx.exifinterface.media.ExifInterface(it) }
                    val matrix = android.graphics.Matrix().apply { postRotate((exif?.rotationDegrees ?: 0).toFloat()); if (exif?.isFlipped == true) postScale(-1f, 1f) }
                    if (decoded == null || matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else null
                currentCoroutineContext().ensureActive()
                synchronized(cache) {
                    if (requestedGeneration != generation.get()) null
                    else bitmap?.also { cache.put(uri.toString(), it) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
        }
    }
}

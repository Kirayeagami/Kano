package app.kano
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import app.kano.platform.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.UUID

/** Owned synthetic image fixtures exercise the real provider; finally removes only created URIs. */
class GalleryRepositoryTest {
    @get:Rule val access: TestRule = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                val permissions = if (Build.VERSION.SDK_INT >= 33)
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
                    else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                try {
                    GrantPermissionRule.grant(*permissions).apply(base, description).evaluate()
                } catch (_: SecurityException) {
                    base.evaluate()
                }
            }
        }
    }
    @Test fun realProviderKeepsSearchAndTypeFiltersAcrossPages() = runBlocking {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 29)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val hasPermission = if (Build.VERSION.SDK_INT >= 33) {
            context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        Assume.assumeTrue(hasPermission)
        val resolver = context.contentResolver
        val prefix = "KANO_TEST_ONLY-" + UUID.randomUUID()
        val owned = mutableListOf<android.net.Uri>()
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            repeat(64) { index ->
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$prefix-$index.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kano-validation")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                })!!
                owned += uri
                resolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                assertEquals(1, resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null))
            }
            val gallery = GalleryRepository(context)
            assertEquals(GalleryAccess.FULL, gallery.access())
            val first = gallery.page(0, GalleryFilter.PHOTOS, prefix)
            val second = gallery.page(60, GalleryFilter.PHOTOS, prefix)
            assertEquals(60, first.items.size); assertTrue(first.hasMore)
            assertEquals(4, second.items.size); assertFalse(second.hasMore)
            assertEquals(64, (first.items + second.items).map { it.uri }.distinct().size)
            assertTrue((first.items + second.items).all { it.name.startsWith(prefix) && !it.isVideo })
            assertTrue(gallery.page(0, GalleryFilter.VIDEOS, prefix).items.isEmpty())
            assertTrue(gallery.page(999, GalleryFilter.PHOTOS, prefix).items.isEmpty())
        } finally { owned.forEach { resolver.delete(it, null, null) }; bitmap.recycle() }
    }
}

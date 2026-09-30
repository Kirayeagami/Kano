package app.kano

import android.Manifest
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.kano.platform.StorageScope
import app.kano.platform.StorageSources
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated provider fixtures: no writes, grants or deletions touch shared storage. */
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class StorageSourcesTest {
    @Test fun realProviderQueriesStayBoundedWithoutChangingSharedStorage() = runBlocking {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 29)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sources = StorageSources(context)
        val scope = sources.scopes().firstOrNull { it.kind == StorageSources.MEDIASTORE }
        Assume.assumeNotNull(scope)
        val page = sources.page(scope!!, 0, null, 17)
        assertTrue(page.items.size <= 17)
        assertEquals(page.items.size, page.items.map { it.identity }.distinct().size)
        assertTrue(page.items.all { it.sourceId > 0 && it.uri.startsWith("content://media/") })
        val visible = sources.visibleIds(scope, 0, 17)
        assertTrue(visible.first.size <= 17)
        assertEquals(visible.first.sorted().distinct(), visible.first)
    }

    @Test fun keysetPagesRemainBoundedAndDisjointWhenProviderIgnoresLimit() = runBlocking {
        val fixture = fixture(10_000)
        val first = fixture.sources.page(fixture.scope, 0, null)
        assertEquals(200, first.items.size)
        assertEquals(200L, first.nextId)
        assertFalse(first.done)
        val second = fixture.sources.page(fixture.scope, first.nextId, null)
        assertEquals(201L, second.items.first().sourceId)
        assertEquals(400L, second.nextId)
        assertTrue(first.items.map { it.identity }.intersect(second.items.map { it.identity }.toSet()).isEmpty())
        assertTrue(fixture.provider.lastSelection!!.contains("_id>?"))
        assertEquals("_id ASC", fixture.provider.lastOrder)
        assertTrue(first.items.all { it.identity == "media:external_primary:${it.sourceId}" })
        assertTrue(fixture.provider.queriedSelections.any { it.startsWith("media_type IN (") && !it.contains("owner_package_name") })
        assertFalse(fixture.provider.queriedSelections.any { it.contains("owner_package_name") && it.contains("media_type IN (?") })
    }

    @Test fun visibilitySweepIncludesRowsOutsideTheGenerationDelta() = runBlocking {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 30)
        val fixture = fixture(450)
        val delta = fixture.sources.page(fixture.scope.copy(generation = 400), 0, 250)
        assertEquals(150, delta.items.size)
        assertEquals(251L, delta.items.first().sourceId)
        assertEquals(400L, delta.nextId)
        assertTrue(delta.done)
        val first = fixture.sources.visibleIds(fixture.scope, 0)
        assertEquals(200, first.first.size)
        assertEquals(1L, first.first.first())
        assertFalse(first.second)
        val last = fixture.sources.visibleIds(fixture.scope, 400)
        assertEquals((401L..450L).toList(), last.first)
        assertTrue(last.second)
    }

    @Test fun nonIncrementalScopeReenumeratesInsteadOfUsingGenerationFilter() = runBlocking {
        val fixture = fixture(3)
        val page = fixture.sources.page(fixture.scope.copy(incremental = false), 0, 2)
        assertEquals(3, page.items.size)
        assertFalse(fixture.provider.lastSelection!!.contains("generation_modified>?"))
    }

    @Test fun sourceInspectionReadsCurrentRevisionAndKeepsMissingMetadataUnknown() = runBlocking {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 30)
        val fixture = fixture(2)
        fixture.provider.missingSize = true
        val before = fixture.sources.inspect("content://media/external_primary/images/media/1", fixture.scope.key)
        assertNull(before.sizeBytes)
        assertNull(before.width)
        assertEquals(1L, before.generation)
        assertEquals(1_001_000L, before.modifiedAt)
        assertEquals("content://media/external_primary/file/1", fixture.provider.lastUri.toString())
        assertEquals("content://media/external_primary/images/media/1", before.uri)
        fixture.provider.revision = 100
        val after = fixture.sources.inspect(before.uri, fixture.scope.key)
        assertEquals(before.identity, after.identity)
        assertEquals(101L, after.generation)
        assertEquals(1_101_000L, after.modifiedAt)
    }

    @Test fun typedCollectionsUseFilesMetadataAndRetainTheAuthorizedReadUri() = runBlocking {
        val fixture = fixture(1)
        for (collection in listOf("images/media", "video/media", "audio/media", "downloads")) {
            val readUri = "content://media/external_primary/$collection/1"
            val entry = fixture.sources.inspect(readUri, fixture.scope.key)
            assertEquals("content://media/external_primary/file/1", fixture.provider.lastUri.toString())
            assertEquals(readUri, entry.uri)
            assertEquals("media:external_primary:1", entry.identity)
        }
    }

    @Test fun deniedRuntimePermissionsStillRestrictQueryToOwnedMediaAndDownloads() = runBlocking {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 33)
        val fixture = fixture(1, allowed = emptySet())
        fixture.sources.page(fixture.scope.copy(incremental = false), 0, null)
        assertTrue(fixture.provider.lastSelection!!.contains("owner_package_name=?"))
        assertTrue(fixture.provider.lastSelection!!.contains("relative_path LIKE 'Download/%'"))
        assertFalse(fixture.provider.lastSelection!!.contains("media_type IN (?,"))
        assertTrue(fixture.sources.permissions().contains(Manifest.permission.READ_MEDIA_AUDIO))
    }

    @Test fun grantedAndOwnedQueriesMergeWithoutGapsDuplicateRowsOrUnownedDocuments() = runBlocking {
        val fixture = fixture(9, setOf(Manifest.permission.READ_MEDIA_IMAGES))
        fixture.provider.ownedIds = setOf(2L, 3L, 4L, 5L, 7L)
        fixture.provider.audioIds = setOf(2L, 4L)
        fixture.provider.otherIds = setOf(3L, 6L)
        fixture.provider.downloadIds = setOf(3L, 6L)
        var after = 0L
        val readIds = mutableListOf<Long>()
        do {
            val page = fixture.sources.page(fixture.scope.copy(incremental = false), after, null, 3)
            val visible = fixture.sources.visibleIds(fixture.scope, after, 3)
            assertEquals(page.items.map { it.sourceId }, visible.first)
            assertEquals(page.done, visible.second)
            assertTrue(page.items.size <= 3)
            readIds += page.items.map { it.sourceId }
            after = page.nextId
        } while (!page.done)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 7L, 8L, 9L), readIds)
        assertEquals(readIds.size, readIds.distinct().size)
    }

    @Test fun deniedAccessEnumeratesOnlyOwnedMediaAndOwnedDownloadDocuments() = runBlocking {
        val fixture = fixture(8, emptySet())
        fixture.provider.ownedIds = setOf(2L, 4L, 7L)
        fixture.provider.audioIds = setOf(2L)
        fixture.provider.otherIds = setOf(4L, 6L)
        fixture.provider.downloadIds = setOf(4L, 6L)
        val page = fixture.sources.page(fixture.scope.copy(incremental = false), 0, null)
        assertEquals(listOf(2L, 4L, 7L), page.items.map { it.sourceId })
        assertTrue(page.done)
        assertTrue(fixture.provider.queriedSelections.all { it.startsWith("owner_package_name=?") })
    }

    @Test fun selectedVisualAccessRequiresReconciliationUnlessBothVisualKindsAreGranted() {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 34)
        val selected = setOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        assertTrue(fixture(0, selected).sources.access().visualSelection)
        assertTrue(fixture(0, selected + Manifest.permission.READ_MEDIA_IMAGES).sources.access().visualSelection)
        assertFalse(fixture(0, selected + setOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)).sources.access().visualSelection)
    }

    private fun fixture(count: Int, allowed: Set<String> = setOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)): Fixture {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 29)
        val base = ApplicationProvider.getApplicationContext<Context>()
        val wrapped = FixtureContext(base, allowed)
        val provider = FixtureProvider(count)
        provider.attachInfo(base, ProviderInfo().apply { authority = MediaStore.AUTHORITY })
        wrapped.resolver = ContentResolver.wrap(provider)
        return Fixture(StorageSources(wrapped), provider,
            StorageScope("ms:external_primary", StorageSources.MEDIASTORE, "content://media/external_primary/file", "fixture-version", count.toLong(), true))
    }

    private data class Fixture(val sources: StorageSources, val provider: FixtureProvider, val scope: StorageScope)
    private class FixtureContext(base: Context, private val allowed: Set<String>) : ContextWrapper(base) {
        lateinit var resolver: ContentResolver
        override fun getApplicationContext(): Context = this
        override fun getContentResolver(): ContentResolver = resolver
        override fun checkPermission(permission: String, pid: Int, uid: Int): Int = if (permission in allowed) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
    }

    private class FixtureProvider(private val fixtureRowCount: Int) : ContentProvider() {
        var lastSelection: String? = null
        var lastOrder: String? = null
        var lastUri: Uri? = null
        val queriedSelections = mutableListOf<String>()
        var revision: Long = 0
        var missingSize = false
        var ownedIds = emptySet<Long>()
        var audioIds = emptySet<Long>()
        var otherIds = emptySet<Long>()
        var downloadIds = emptySet<Long>()
        override fun onCreate() = true
        override fun getType(uri: Uri) = "image/png"
        override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("Read-only fixture")
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("Read-only fixture")
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("Read-only fixture")

        override fun query(uri: Uri, projection: Array<out String>?, queryArgs: Bundle?, cancellationSignal: CancellationSignal?): Cursor =
            query(uri, projection, queryArgs?.getString(ContentResolver.QUERY_ARG_SQL_SELECTION),
                queryArgs?.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS), queryArgs?.getString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER))

        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
            lastSelection = selection; lastOrder = sortOrder; lastUri = uri
            selection?.let { queriedSelections += it }
            require(uri.pathSegments.size >= 2 && uri.pathSegments[1] == "file") { "Fixture requires Files metadata queries" }
            val columns = projection ?: arrayOf("_id")
            fun argument(clause: String): Long? {
                val index = selection?.indexOf(clause) ?: return null
                if (index < 0) return null
                val argIndex = selection.substring(0, index).count { it == '?' }
                return selectionArgs?.getOrNull(argIndex)?.toLongOrNull()
            }
            val after = argument("_id>?") ?: 0
            val since = argument("generation_modified>?")
            val upper = argument("generation_modified<=?")
            val singleton = uri.lastPathSegment?.toLongOrNull()
            val ownedQuery = selection?.startsWith("owner_package_name=?") == true
            val publicQuery = selection?.startsWith("media_type IN (") == true
            val publicTypeCount = if (publicQuery) selection!!.substringBefore(')').count { it == '?' } else 0
            val publicTypes = selectionArgs?.take(publicTypeCount)?.mapNotNull { it.toLongOrNull() }.orEmpty()
            // Reproduce the observed OEM behavior: combining public types with owner filtering loses rows.
            val mixedOwnerQuery = selection?.contains("owner_package_name=?") == true && selection.contains("media_type IN (?")
            return MatrixCursor(columns).apply {
                for (id in 1L..fixtureRowCount.toLong()) {
                    if (mixedOwnerQuery) continue
                    if (singleton != null && id != singleton) continue
                    if (id <= after || (since != null && id + revision <= since) || (upper != null && id + revision > upper)) continue
                    val type = when (id) { in audioIds -> 2L; in otherIds -> 0L; else -> 1L }
                    if (ownedQuery && (id !in ownedIds || (type !in 1L..3L && id !in downloadIds))) continue
                    if (publicQuery && type !in publicTypes) continue
                    addRow(columns.map { column -> when (column) {
                        "_id" -> id
                        "_display_name" -> "fixture-$id.png"
                        "mime_type" -> when (type) { 2L -> "audio/ogg"; 0L -> "application/octet-stream"; else -> "image/png" }
                        "_size" -> if (missingSize) null else 1_024L + id
                        "date_added" -> 1_000L
                        "date_modified" -> 1_000L + id + revision
                        "media_type" -> type
                        "volume_name" -> "external_primary"
                        "relative_path" -> if (id in downloadIds) "Download/Test-only/" else "Pictures/Test-only/"
                        "generation_modified" -> id + revision
                        else -> null
                    } }.toTypedArray())
                }
            }
        }
    }
}

package app.kano

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import app.kano.core.ContentHasher
import app.kano.core.HashResult
import app.kano.platform.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.After

/** Real-provider reads; resume updates Kano's index only. Never writes/deletes originals. */
class PhysicalStorageVerificationTest {
    @get:Rule val activity = ActivityScenarioRule(MainActivity::class.java)
    @Before fun keepTestActivityAwake() {
        activity.scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    @After fun restoreTestActivity() {
        activity.scenario.onActivity { it.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @Test fun storageMetadataCoversVisibleGalleryItems() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sources = StorageSources(context)
        val scopes = sources.scopes().filter { it.kind == StorageSources.MEDIASTORE }
        assertTrue("A mounted MediaStore source must be discoverable", scopes.isNotEmpty())
        val gallery = GalleryRepository(context)
        val galleryCount = if (gallery.access() != GalleryAccess.DENIED) gallery.page(0, GalleryFilter.ALL, "").items.size else 0
        var count = 0
        scopes.forEach { scope ->
            val page = sources.page(scope, 0, null)
            val visibility = sources.visibleIds(scope, 0)
            assertEquals("Metadata and visibility queries must agree", page.items.map { it.sourceId }, visibility.first)
            assertTrue("Provider batches remain bounded", page.items.size <= 200)
            count += page.items.size
        }
        println("STORAGE_GALLERY_FIRST_PAGE=$galleryCount STORAGE_METADATA_FIRST_BATCH=$count")
        assertTrue("Storage must include currently visible Gallery items", count >= galleryCount)
    }

    @Test fun finishedOnePlusIndexMatchesProviderAndVerifiedBytes(): Unit = runBlocking {
        assumeTrue("Primary physical target only; never seed personal storage", Build.MODEL == "CPH2717")
        val application = ApplicationProvider.getApplicationContext<KanoApplication>()
        val graph = application.graph
        val dao = graph.database.storage()
        val access = graph.storageSources.access()
        assumeTrue("Physical real-media index verification requires authorized media access on CPH2717", access.hasSources)
        val initial = dao.scan()
        if (initial == null || initial.status in setOf("INTERRUPTED", "CANCELLED", "PAUSED", "ERROR")) {
            graph.storage.requestScan(resume = initial?.status == "INTERRUPTED")
            graph.storage.runScan()
        }
        // Instrumentation restarts the target process. Allow its already-authorized
        // WorkManager job to recover instead of testing a transient SCANNING row.
        val scan = withTimeout(150_000) {
            var current = dao.scan()
            while (current == null || current.status in setOf("QUEUED", "SCANNING", "INTERRUPTED")) {
                delay(500)
                current = dao.scan()
            }
            requireNotNull(current)
        }
        println("STORAGE_PHYSICAL_STATUS=${scan.status} reads=${scan.scanned} hashes=${scan.hashed} errors=${scan.errors}")
        if (scan.status == "STALE_INDEX") {
            val changed = graph.storageSources.scopes().filter { it.kind == StorageSources.MEDIASTORE }.any { scope ->
                val saved = dao.checkpoint(scope.key)
                saved?.version != scope.version || saved?.generation != scope.generation
            }
            assertTrue("Stale state must be justified by an actual provider revision change", changed)
            val snapshot = dao.totals().first()
            println("STORAGE_PHYSICAL_STALE count=${snapshot.count} bytes=${snapshot.bytes} providerChanged=$changed")
            assumeTrue("Fresh final scan UNVERIFIED: MediaStore changed during comparison", false)
        }
        assertTrue("The actual user scan must finish or explicitly report unreadable candidates", scan.status in setOf("COMPLETED", "PARTIAL_FAILURE"))
        assertEquals(graph.storageSources.access().signature, scan.scopeSignature)
        val hashStates = linkedMapOf<String, Long>()
        graph.database.openHelper.readableDatabase.query("SELECT hashState, COUNT(*) FROM storage_entries WHERE sizeBytes>0 GROUP BY hashState").use { cursor ->
            while (cursor.moveToNext()) hashStates[cursor.getString(0)] = cursor.getLong(1)
        }
        println("STORAGE_PHYSICAL_HASH_STATES=$hashStates")
        assertEquals("Any partial result must be explained by failed candidates, not lost metadata", scan.errors,
            hashStates.filterKeys { it in setOf("ERROR", "SOURCE_CHANGED", "REVOKED") }.values.sum())
        var mediaCount = 0L
        for (scope in graph.storageSources.scopes().filter { it.kind == StorageSources.MEDIASTORE }) {
            var cursor = 0L
            do {
                val batch = graph.storageSources.visibleIds(scope, cursor)
                mediaCount += batch.first.size
                if (batch.first.isNotEmpty()) cursor = batch.first.last()
            } while (!batch.second)
        }
        val totals = dao.totals().first()
        assertTrue("A real-media index must contain the complete accessible set", totals.count >= mediaCount)
        graph.database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM storage_entries WHERE scope LIKE 'ms:%' AND accessState='AUTHORIZED'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Persisted MediaStore count must exactly match public-provider visibility", mediaCount, cursor.getLong(0))
        }
        assertEquals(totals.count, dao.categories().first().sumOf { it.count })
        println("STORAGE_PHYSICAL_INDEX count=${totals.count} provider=$mediaCount bytes=${totals.bytes} duplicateGroupsShown=${dao.duplicates().first().size} hashes=${scan.hashed} errors=${scan.errors}")
        assumeTrue("Physical real-media index verification requires accessible media on CPH2717", mediaCount > 0)
        val duplicates = dao.duplicates().first()
        duplicates.minByOrNull { it.sizeBytes }?.let { group ->
            val members = dao.duplicateMembers(group.sha256).take(2)
            assertEquals(2, members.size)
            for (member in members) {
                val fresh = graph.storageSources.inspect(member.uri, member.scope)
                assertEquals(member.sizeBytes, fresh.sizeBytes)
                val hash = application.contentResolver.openInputStream(android.net.Uri.parse(member.uri))!!.use {
                    ContentHasher.hash(it, requireNotNull(member.sizeBytes), Long.MAX_VALUE) { }
                }
                assertTrue(hash is HashResult.Complete)
                assertEquals(group.sha256, (hash as HashResult.Complete).sha256)
            }
        }
    }
}

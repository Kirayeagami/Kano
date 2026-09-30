package app.kano

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import app.kano.data.*
import app.kano.platform.StorageSources
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.UUID

/** Emulator fixture files only. The finally block removes only URIs inserted by this test. */
class StorageRealScanTest {
    @Test fun actualMediaStoreBytesProduceExactHashEvidenceAndIncrementalIndex() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= 29 && Build.MODEL.contains("SDK", ignoreCase=true))
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = context.contentResolver
        val owned = mutableListOf<android.net.Uri>()
        val db = Room.inMemoryDatabaseBuilder(context, KanoDatabase::class.java).build()
        try {
            val prefix = "kano-fixture-" + UUID.randomUUID()
            val bytes = ByteArray(4_096) { (it % 251).toByte() }
            repeat(2) { copy ->
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$prefix-$copy.bin")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/KanoValidation/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                })!!
                owned += uri
                resolver.openOutputStream(uri)!!.use { it.write(bytes) }
                resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            }
            val sources = StorageSources(context)
            val repo = StorageRepository(context, db, sources, WorkManager.getInstance(context))
            db.storage().saveScan(StorageScan(status="QUEUED", runId=101, scopeSignature=sources.access().signature))
            repo.runScan()
            val rows = owned.map { sources.inspect(it.toString(), "ms:external_primary") }.map { db.storage().findIdentity(it.identity)!! }
            assertEquals(2, rows.size)
            assertTrue(rows.all { it.sizeBytes == bytes.size.toLong() && it.hashState == "VERIFIED" })
            assertEquals(rows.first().sha256, rows.last().sha256)
            val match = db.storage().duplicates().first().first { it.sha256 == rows.first().sha256 }
            assertTrue(match.copies >= 2)
            assertEquals(4_096L, match.sizeBytes)
            db.storage().saveScan(StorageScan(status="QUEUED", runId=102, scopeSignature=sources.access().signature))
            repo.runScan()
            assertTrue(db.storage().scan()!!.scanned <= db.storage().count())
        } finally {
            db.close()
            owned.forEach { resolver.delete(it, null, null) }
        }
    }
}

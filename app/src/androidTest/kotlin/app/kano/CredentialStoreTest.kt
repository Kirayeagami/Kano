package app.kano

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.kano.core.CredentialRead
import app.kano.core.CredentialRemoval
import app.kano.core.CredentialSlot
import app.kano.core.CredentialWrite
import app.kano.security.KeystoreCredentialStore
import java.io.File
import java.security.KeyStore
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CredentialStoreTest {
    private lateinit var directory: File
    private lateinit var alias: String
    private lateinit var store: KeystoreCredentialStore
    private val slot = CredentialSlot.OPENAI
    private val fixture = "synthetic-test-credential".toByteArray()

    @Before fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        directory = File(context.cacheDir, "credential-test-$id")
        alias = "kano.test.$id"
        store = KeystoreCredentialStore(directory, alias)
    }
    @After fun cleanup() {
        directory.deleteRecursively()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
    }
    @Test fun encryptedRoundTripSurvivesAdapterRecreation() = runBlocking {
        assertEquals(CredentialWrite.SAVED, store.save(slot, fixture))
        assertFalse(File(directory, "OPENAI.bin").readBytes().toString(Charsets.ISO_8859_1).contains("synthetic-test-credential"))
        val reopened = KeystoreCredentialStore(directory, alias).read(slot)
        assertTrue(reopened is CredentialRead.Found)
        val bytes = (reopened as CredentialRead.Found).bytes
        try { assertArrayEquals(fixture, bytes); assertEquals("CredentialRead.Found([redacted])", reopened.toString()) }
        finally { bytes.fill(0) }
    }
    @Test fun tamperAndCrossProviderSubstitutionFailClosed() = runBlocking {
        store.save(slot, fixture)
        val source = File(directory, "OPENAI.bin")
        source.copyTo(File(directory, "GEMINI.bin"))
        assertEquals(CredentialRead.Corrupt, store.read(CredentialSlot.GEMINI))
        val damaged = source.readBytes()
        damaged[damaged.lastIndex] = (damaged.last().toInt() xor 1).toByte()
        source.writeBytes(damaged)
        assertEquals(CredentialRead.Corrupt, store.read(slot))
    }
    @Test fun repeatedSaveUsesFreshNonce() = runBlocking {
        store.save(slot, fixture)
        val first = File(directory, "OPENAI.bin").readBytes()
        store.save(slot, fixture)
        assertFalse(first.contentEquals(File(directory, "OPENAI.bin").readBytes()))
    }
    @Test fun missingKeyDoesNotRegenerateOrOverwriteCiphertext() = runBlocking {
        store.save(slot, fixture)
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
        assertEquals(CredentialRead.Unavailable, store.read(slot))
        assertTrue(File(directory, "OPENAI.bin").exists())
    }
    @Test fun truncatedInputFailsWithoutUnboundedAllocation() = runBlocking {
        directory.mkdirs()
        File(directory, "OPENAI.bin").writeBytes(byteArrayOf(1, 2))
        assertEquals(CredentialRead.Corrupt, store.read(slot))
    }
    @Test fun removalIsIdempotentAndInvalidInputsAreRejected() = runBlocking {
        assertEquals(CredentialRead.Missing, store.read(slot))
        assertEquals(CredentialWrite.INVALID_INPUT, store.save(slot, byteArrayOf()))
        assertEquals(CredentialWrite.INVALID_INPUT, store.save(slot, ByteArray(KeystoreCredentialStore.MAX_SECRET_BYTES + 1)))
        store.save(slot, fixture)
        assertEquals(CredentialRemoval.REMOVED, store.remove(slot))
        assertEquals(CredentialRead.Missing, store.read(slot))
        assertEquals(CredentialRemoval.REMOVED, store.remove(slot))
    }
}

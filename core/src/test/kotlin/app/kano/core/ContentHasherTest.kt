package app.kano.core

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentHasherTest {
    @Test fun emptyFileHasRealSha256() {
        assertEquals(HashResult.Complete("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            ContentHasher.hash(ByteArrayInputStream(byteArrayOf()), 0, 8) {})
    }
    @Test fun exactLimitIsIncluded() {
        assertEquals(HashResult.Complete(PrivacyFirewall.digest("abc")), ContentHasher.hash(ByteArrayInputStream("abc".toByteArray()), 3, 3) {})
    }
    @Test fun misleadingSmallSizeDoesNotBypassByteLimit() {
        assertEquals(HashResult.LimitExceeded, ContentHasher.hash(ByteArrayInputStream(ByteArray(100)), 2, 10) {})
    }
    @Test fun mismatchedSizeIsNeverPresentedAsComplete() {
        assertEquals(HashResult.SourceChanged, ContentHasher.hash(ByteArrayInputStream(ByteArray(3)), 4, 10) {})
    }
    @Test(expected = CancellationException::class) fun cancellationInterruptsBeforeReading() {
        ContentHasher.hash(ByteArrayInputStream(ByteArray(100)), 100, 100) { throw CancellationException() }
    }
    @Test(expected = IOException::class) fun providerErrorsPropagateToRepository() {
        val input = object : InputStream() { override fun read(): Int = throw IOException("test failure") }
        ContentHasher.hash(input, 1, 10) {}
    }
    @Test(expected = IOException::class) fun noProgressCannotSpinForever() {
        val input = object : InputStream() {
            override fun read(): Int = 0
            override fun read(buffer: ByteArray): Int = 0
        }
        ContentHasher.hash(input, 1, 10) {}
    }
}

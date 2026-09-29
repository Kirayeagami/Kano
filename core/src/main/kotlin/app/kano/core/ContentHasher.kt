package app.kano.core

import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

sealed interface HashResult {
    data class Complete(val sha256: String) : HashResult
    data object LimitExceeded : HashResult
    data object SourceChanged : HashResult
}

/** Does not own/close the input; caller controls its lifecycle and cancellation. */
object ContentHasher {
    fun hash(input: InputStream, expectedBytes: Long, limitBytes: Long, checkActive: () -> Unit): HashResult {
        require(expectedBytes >= 0 && limitBytes >= 0)
        if (expectedBytes > limitBytes) return HashResult.LimitExceeded
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            checkActive()
            val read = input.read(buffer)
            if (read == -1) break
            if (read == 0) throw IOException("Source made no progress")
            total += read
            if (total > limitBytes) return HashResult.LimitExceeded
            digest.update(buffer, 0, read)
        }
        checkActive()
        if (total != expectedBytes) return HashResult.SourceChanged
        return HashResult.Complete(digest.digest().joinToString("") { "%02x".format(it) })
    }
}

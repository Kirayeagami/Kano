package app.kano.platform

import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Call on Dispatchers.IO. Cancellation immediately signals a supporting content provider. */
internal suspend fun <T> cancellableProviderRead(read: (CancellationSignal) -> T): T =
    suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        try {
            val result = read(signal)
            if (continuation.isActive) continuation.resume(result)
        } catch (failure: Exception) {
            if (continuation.isActive) continuation.resumeWithException(failure)
        }
    }

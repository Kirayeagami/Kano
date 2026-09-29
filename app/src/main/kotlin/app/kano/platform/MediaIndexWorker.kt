package app.kano.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.kano.KanoApplication
import kotlinx.coroutines.CancellationException

class MediaIndexWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        (applicationContext as KanoApplication).graph.media.scan()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        // Exception messages can contain a private URI or document name.
        Result.failure(workDataOf("reason" to "INDEX_FAILED"))
    }
}

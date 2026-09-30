package app.kano.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.kano.KanoApplication
import kotlinx.coroutines.CancellationException

class StorageIndexWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        (applicationContext as KanoApplication).graph.storage.runScan()
        Result.success()
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { if (runAttemptCount < 2) Result.retry() else Result.failure() }
}

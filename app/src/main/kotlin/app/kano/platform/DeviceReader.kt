package app.kano.platform

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceSnapshot(
    val model: String,
    val androidVersion: String,
    val storageTotal: Long,
    val storageAvailable: Long,
    val memoryTotal: Long?,
    val memoryAvailable: Long?,
    val batteryPercent: Int?,
    val charging: Boolean?,
    val capturedAt: Long,
)

class DeviceReader(private val context: Context) {
    suspend fun read(): DeviceSnapshot = withContext(Dispatchers.IO) {
        val storage = StatFs(context.filesDir.absolutePath)
        val memory = context.getSystemService(ActivityManager::class.java)?.let { manager ->
            ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        }
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        DeviceSnapshot(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
            storageTotal = storage.totalBytes,
            storageAvailable = storage.availableBytes,
            memoryTotal = memory?.totalMem,
            memoryAvailable = memory?.availMem,
            batteryPercent = if (level >= 0 && scale > 0) ((level.toLong() * 100) / scale).toInt().coerceIn(0, 100) else null,
            charging = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
                BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
                else -> null
            },
            capturedAt = System.currentTimeMillis(),
        )
    }
}

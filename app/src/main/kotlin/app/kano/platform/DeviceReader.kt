package app.kano.platform

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

enum class MetricFreshness {
    LIVE,
    UPDATED_RECENTLY,
    STALE,
    UNAVAILABLE,
}

data class DeviceMetric<T>(
    val value: T?,
    val freshness: MetricFreshness = MetricFreshness.LIVE,
    val label: String,
    val source: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val buildId: String,
    val cpuAbi: String,
    val cpuCores: Int,
    val storageTotal: Long,
    val storageAvailable: Long,
    val memoryTotal: Long?,
    val memoryAvailable: Long?,
    val memoryLow: Boolean,
    val batteryPercent: Int?,
    val charging: Boolean?,
    val batteryTemperatureC: Double?,
    val batteryVoltageMv: Int?,
    val connectionType: String,
    val bluetoothEnabled: Boolean?,
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
        val tempTenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltageMv = battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

        val connType = try {
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            @Suppress("MissingPermission")
            val activeNetwork = connectivityManager?.activeNetwork
            @Suppress("MissingPermission")
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            when {
                capabilities == null -> "Offline"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Connected"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Network"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Connected"
            }
        } catch (_: SecurityException) {
            "Permission Not Requested (Local Only)"
        }

        val bluetoothEnabled = try {
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            bluetoothManager?.adapter?.isEnabled
        } catch (_: SecurityException) {
            null
        }

        DeviceSnapshot(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Unavailable",
            buildId = Build.DISPLAY,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            storageTotal = storage.totalBytes,
            storageAvailable = storage.availableBytes,
            memoryTotal = memory?.totalMem,
            memoryAvailable = memory?.availMem,
            memoryLow = memory?.lowMemory ?: false,
            batteryPercent = if (level >= 0 && scale > 0) ((level.toLong() * 100) / scale).toInt().coerceIn(0, 100) else null,
            charging = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
                BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
                else -> null
            },
            batteryTemperatureC = if (tempTenths > 0) tempTenths / 10.0 else null,
            batteryVoltageMv = if (voltageMv > 0) voltageMv else null,
            connectionType = connType,
            bluetoothEnabled = bluetoothEnabled,
            capturedAt = System.currentTimeMillis(),
        )
    }

    /** Emits live device metric snapshots on a periodic sampling loop while subscribed. */
    fun observeLiveMetrics(): Flow<DeviceSnapshot> = flow {
        while (true) {
            emit(read())
            delay(3_000) // Sample every 3 seconds
        }
    }.flowOn(Dispatchers.IO)
}

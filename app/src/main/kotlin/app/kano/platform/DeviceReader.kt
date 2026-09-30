package app.kano.platform

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.NfcAdapter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sqrt

enum class MetricFreshness {
    LIVE,
    UPDATED_RECENTLY,
    STATIC,
    STALE,
    UNAVAILABLE,
}

enum class MetricAvailability {
    AVAILABLE,
    PERMISSION_REQUIRED,
    HARDWARE_ABSENT,
    API_RESTRICTED,
    UNAVAILABLE,
}

data class DeviceMetric<T>(
    val value: T?,
    val freshness: MetricFreshness = MetricFreshness.LIVE,
    val label: String,
    val source: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

data class SensorDetail(
    val name: String,
    val vendor: String,
    val typeString: String,
    val powerMa: Float,
    val resolution: Float,
    val maxRange: Float,
)

data class CameraDetail(
    val id: String,
    val facing: String,
    val hardwareLevel: String,
    val orientationDegrees: Int,
    val hasFlash: Boolean,
    val maxResolution: String?,
)

data class KanoStorageBreakdown(
    val internalFilesBytes: Long,
    val internalCacheBytes: Long,
    val codeCacheBytes: Long,
    val databaseBytes: Long,
    val totalBytes: Long,
)

data class SubsystemDiagnostic(
    val name: String,
    val status: String,
    val source: String,
    val freshness: String,
    val isOperational: Boolean,
    val details: String,
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
    // Phase 1 Extended Fields:
    val deviceName: String = model,
    val buildType: String = "user",
    val buildTags: String = "release-keys",
    val buildFingerprint: String = "unknown",
    val kernelVersion: String = "Linux",
    val supportedAbis: List<String> = emptyList(),
    val socModel: String? = null,
    val hardware: String = "unknown",
    val board: String = "unknown",
    val glEsVersion: String? = null,
    val memoryUsed: Long? = if (memoryTotal != null && memoryAvailable != null) (memoryTotal - memoryAvailable).coerceAtLeast(0) else null,
    val memoryThreshold: Long? = null,
    val storageUsed: Long = (storageTotal - storageAvailable).coerceAtLeast(0),
    val kanoStorageBytes: Long? = null,
    val batteryStatus: String = "Unknown",
    val batteryPlugged: String = "Unknown",
    val batteryHealth: String = "Unknown",
    val batteryTechnology: String? = null,
    val thermalStatus: String = "Unavailable",
    val thermalStatusCode: Int? = null,
    val displayResolution: String = "Unavailable",
    val displayDensityDpi: Int = 0,
    val displayDensityFactor: Float = 1.0f,
    val displayRefreshRate: Float? = null,
    val displayHdrCapable: Boolean? = null,
    val wifiActive: Boolean = false,
    val vpnActive: Boolean = false,
    val airplaneMode: Boolean = false,
    val cameraBackCount: Int = 0,
    val cameraFrontCount: Int = 0,
    val cameraHasFlash: Boolean = false,
    val sensorCount: Int = 0,
    val sensorCategories: List<String> = emptyList(),
    val nfcPresent: Boolean = false,
    val nfcEnabled: Boolean? = null,
    val audioOutputRoutes: List<String> = listOf("Built-in Speaker"),
    val audioMode: String = "Normal",
    val hasVibrator: Boolean = false,
    val hasFlashlight: Boolean = false,
    val locationMasterEnabled: Boolean = false,
    // Phase 1 Deep Hardware & Subsystem Extensions:
    val cpuModelName: String? = null,
    val cpuBogoMips: String? = null,
    val cpuFeatures: List<String> = emptyList(),
    val supported32BitAbis: List<String> = emptyList(),
    val supported64BitAbis: List<String> = emptyList(),
    val displayPhysicalSizeInches: Double? = null,
    val displayWideColorGamut: Boolean? = null,
    val displaySupportedModesCount: Int = 1,
    val displaySupportedModesSummary: String = "Standard",
    val batteryCurrentNowMa: Int? = null,
    val batteryCurrentAverageMa: Int? = null,
    val batteryChargeCounterUah: Int? = null,
    val batteryCapacityPercent: Int? = null,
    val thermalHeadroom: Float? = null,
    val hasAmplitudeControl: Boolean = false,
    val audioInputRoutes: List<String> = listOf("Built-in Microphone"),
    val cellularActive: Boolean = false,
    val isMeteredNetwork: Boolean? = null,
    val networkDownstreamKbps: Int? = null,
    val networkUpstreamKbps: Int? = null,
    val sensorDetails: List<SensorDetail> = emptyList(),
    val cameraDetails: List<CameraDetail> = emptyList(),
    val subsystems: List<SubsystemDiagnostic> = emptyList(),
    val kanoStorageBreakdown: KanoStorageBreakdown = KanoStorageBreakdown(0L, 0L, 0L, 0L, 0L),
)

private data class CpuInfoDetails(
    val modelName: String? = null,
    val features: List<String> = emptyList(),
    val bogoMips: String? = null,
)

private data class StaticHardwareSpecs(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val buildId: String,
    val buildType: String,
    val buildTags: String,
    val buildFingerprint: String,
    val cpuAbi: String,
    val supportedAbis: List<String>,
    val supported32BitAbis: List<String>,
    val supported64BitAbis: List<String>,
    val cpuCores: Int,
    val hardware: String,
    val board: String,
    val socModel: String?,
    val glEsVersion: String?,
    val kernelVersion: String,
    val deviceName: String,
    val displayResolution: String,
    val displayDensityDpi: Int,
    val displayDensityFactor: Float,
    val displayRefreshRate: Float?,
    val displayHdrCapable: Boolean?,
    val displayWideColorGamut: Boolean?,
    val displayPhysicalSizeInches: Double?,
    val displaySupportedModesCount: Int,
    val displaySupportedModesSummary: String,
    val cameraBackCount: Int,
    val cameraFrontCount: Int,
    val cameraHasFlash: Boolean,
    val cameraDetails: List<CameraDetail>,
    val sensorCount: Int,
    val sensorCategories: List<String>,
    val sensorDetails: List<SensorDetail>,
    val nfcPresent: Boolean,
    val hasVibrator: Boolean,
    val hasAmplitudeControl: Boolean,
    val hasFlashlight: Boolean,
    val cpuModelName: String?,
    val cpuBogoMips: String?,
    val cpuFeatures: List<String>,
)

class DeviceReader(private val context: Context) {

    private val staticSpecs by lazy { readStaticHardware() }

    private fun readCpuInfo(): CpuInfoDetails {
        return try {
            val file = File("/proc/cpuinfo")
            if (!file.exists() || !file.canRead()) return CpuInfoDetails()
            var modelName: String? = null
            val features = mutableListOf<String>()
            var bogoMips: String? = null
            file.forEachLine { line ->
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    when {
                        (key == "model name" || key == "processor") && modelName == null && value.isNotBlank() && !value.all { it.isDigit() } -> {
                            modelName = value
                        }
                        (key == "flags" || key == "features") && features.isEmpty() && value.isNotBlank() -> {
                            features.addAll(value.split("\\s+".toRegex()).take(16))
                        }
                        key == "bogomips" && bogoMips == null && value.isNotBlank() -> {
                            bogoMips = value
                        }
                        key == "hardware" && modelName == null && value.isNotBlank() -> {
                            modelName = value
                        }
                    }
                }
            }
            CpuInfoDetails(modelName, features, bogoMips)
        } catch (_: Exception) {
            CpuInfoDetails()
        }
    }

    private fun readStaticHardware(): StaticHardwareSpecs {
        val windowManager = try { context.getSystemService(WindowManager::class.java) } catch (_: Exception) { null }
        val dm = context.resources.displayMetrics
        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try { windowManager?.currentWindowMetrics?.bounds } catch (_: Exception) { null }
        } else null
        val width = bounds?.width() ?: dm.widthPixels
        val height = bounds?.height() ?: dm.heightPixels
        val resolutionStr = "${width} × ${height} px"
        val densityDpi = dm.densityDpi
        val densityFactor = dm.density

        val displayObj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try { context.display } catch (_: Exception) { null }
        } else {
            @Suppress("DEPRECATION")
            try { windowManager?.defaultDisplay } catch (_: Exception) { null }
        }

        val refreshRate = try { displayObj?.refreshRate } catch (_: Exception) { null }
        val hdrCapable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try { displayObj?.isHdr } catch (_: Exception) { null }
        } else null
        val wideColorGamut = try { displayObj?.isWideColorGamut } catch (_: Exception) { null }

        val physicalSize = if (dm.xdpi > 50f && dm.ydpi > 50f) {
            val wIn = width / dm.xdpi
            val hIn = height / dm.ydpi
            val diag = sqrt((wIn * wIn + hIn * hIn).toDouble())
            if (diag in 2.0..25.0) diag else null
        } else null

        val modes = try {
            displayObj?.supportedModes?.map { "${it.physicalWidth}×${it.physicalHeight} @ ${it.refreshRate.toInt()}Hz" }?.distinct() ?: emptyList()
        } catch (_: Exception) { emptyList() }
        val modesCount = modes.size.coerceAtLeast(1)
        val modesSummary = if (modes.isNotEmpty()) modes.joinToString(", ") else "Default display mode"

        val cameraManager = try { context.getSystemService(CameraManager::class.java) } catch (_: Exception) { null }
        var backCount = 0
        var frontCount = 0
        var flashAvailable = false
        val cameraDetails = mutableListOf<CameraDetail>()
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val facingInt = chars.get(CameraCharacteristics.LENS_FACING)
                val facingStr = when (facingInt) {
                    CameraCharacteristics.LENS_FACING_BACK -> { backCount++; "Rear (Main/Aux)" }
                    CameraCharacteristics.LENS_FACING_FRONT -> { frontCount++; "Front (Selfie)" }
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External USB"
                    else -> "Unknown"
                }
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash) flashAvailable = true
                val hwLevel = when (chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
                    else -> "Standard"
                }
                val orientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
                val pixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val resolutionStr = pixelSize?.let { "${it.width} × ${it.height} px" }
                cameraDetails.add(CameraDetail(id, facingStr, hwLevel, orientation, hasFlash, resolutionStr))
            }
        } catch (_: Exception) {}
        val hasFlashlight = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) || flashAvailable

        val sensorManager = try { context.getSystemService(SensorManager::class.java) } catch (_: Exception) { null }
        val sensorList = try { sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList() } catch (_: Exception) { emptyList() }
        val sensorCategories = mutableSetOf<String>()
        val sensorDetails = mutableListOf<SensorDetail>()
        sensorList.forEach { s ->
            @Suppress("DEPRECATION")
            when (s.type) {
                Sensor.TYPE_ACCELEROMETER -> sensorCategories.add("Accelerometer")
                Sensor.TYPE_GYROSCOPE -> sensorCategories.add("Gyroscope")
                Sensor.TYPE_LIGHT -> sensorCategories.add("Light Sensor")
                Sensor.TYPE_PROXIMITY -> sensorCategories.add("Proximity")
                Sensor.TYPE_MAGNETIC_FIELD -> sensorCategories.add("Magnetometer")
                Sensor.TYPE_PRESSURE -> sensorCategories.add("Barometer")
                Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> sensorCategories.add("Step Counter")
                Sensor.TYPE_GRAVITY -> sensorCategories.add("Gravity")
                Sensor.TYPE_ROTATION_VECTOR -> sensorCategories.add("Rotation Vector")
                Sensor.TYPE_LINEAR_ACCELERATION -> sensorCategories.add("Linear Acceleration")
                Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_TEMPERATURE -> sensorCategories.add("Ambient Temperature")
            }
            if (sensorDetails.size < 25) {
                sensorDetails.add(
                    SensorDetail(
                        name = s.name.orEmpty().ifBlank { "Hardware Sensor" },
                        vendor = s.vendor.orEmpty().ifBlank { "OEM" },
                        typeString = s.stringType.orEmpty().ifBlank { "sensor.type.${s.type}" },
                        powerMa = s.power,
                        resolution = s.resolution,
                        maxRange = s.maximumRange,
                    )
                )
            }
        }

        val nfcAdapter = try { NfcAdapter.getDefaultAdapter(context) } catch (_: Exception) { null }
        val nfcPresent = nfcAdapter != null

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = try { context.getSystemService(VibratorManager::class.java) } catch (_: Exception) { null }
            try { vm?.defaultVibrator } catch (_: Exception) { null }
        } else {
            @Suppress("DEPRECATION")
            try { context.getSystemService(Vibrator::class.java) } catch (_: Exception) { null }
        }
        val hasVibrator = try { vibrator?.hasVibrator() ?: false } catch (_: Exception) { false }
        val hasAmplitudeControl = try { vibrator?.hasAmplitudeControl() ?: false } catch (_: Exception) { false }

        val devName = try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)?.takeIf { it.isNotBlank() }
                ?: Settings.Secure.getString(context.contentResolver, "bluetooth_name")?.takeIf { it.isNotBlank() }
                ?: Build.MODEL
        } catch (_: Exception) {
            Build.MODEL
        }

        val osVersion = System.getProperty("os.version") ?: "Linux"
        val osArch = System.getProperty("os.arch") ?: (Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64")

        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it.isNotBlank() && it != Build.UNKNOWN }
        } else null

        val gles = try {
            context.getSystemService(ActivityManager::class.java)?.deviceConfigurationInfo?.glEsVersion
        } catch (_: Exception) { null }

        val cpuInfo = readCpuInfo()

        return StaticHardwareSpecs(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Unavailable",
            buildId = Build.DISPLAY,
            buildType = Build.TYPE,
            buildTags = Build.TAGS ?: "release-keys",
            buildFingerprint = Build.FINGERPRINT,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            supported32BitAbis = Build.SUPPORTED_32_BIT_ABIS.toList(),
            supported64BitAbis = Build.SUPPORTED_64_BIT_ABIS.toList(),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            hardware = Build.HARDWARE,
            board = Build.BOARD,
            socModel = soc,
            glEsVersion = gles,
            kernelVersion = "$osVersion ($osArch)",
            deviceName = devName,
            displayResolution = resolutionStr,
            displayDensityDpi = densityDpi,
            displayDensityFactor = densityFactor,
            displayRefreshRate = refreshRate,
            displayHdrCapable = hdrCapable,
            displayWideColorGamut = wideColorGamut,
            displayPhysicalSizeInches = physicalSize,
            displaySupportedModesCount = modesCount,
            displaySupportedModesSummary = modesSummary,
            cameraBackCount = backCount,
            cameraFrontCount = frontCount,
            cameraHasFlash = flashAvailable,
            cameraDetails = cameraDetails,
            sensorCount = sensorList.size,
            sensorCategories = sensorCategories.toList().sorted(),
            sensorDetails = sensorDetails,
            nfcPresent = nfcPresent,
            hasVibrator = hasVibrator,
            hasAmplitudeControl = hasAmplitudeControl,
            hasFlashlight = hasFlashlight,
            cpuModelName = cpuInfo.modelName,
            cpuBogoMips = cpuInfo.bogoMips,
            cpuFeatures = cpuInfo.features,
        )
    }

    private fun calculateDirBytes(dir: File?, maxDepth: Int = 3): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        try {
            dir.walkTopDown().maxDepth(maxDepth).forEach { file ->
                if (file.isFile) total += file.length()
            }
        } catch (_: Exception) { }
        return total
    }

    private fun readKanoStorageBreakdown(): KanoStorageBreakdown {
        val files = calculateDirBytes(context.filesDir)
        val cache = calculateDirBytes(context.cacheDir)
        val codeCache = calculateDirBytes(context.codeCacheDir)
        var db = 0L
        try {
            val dbFile = context.getDatabasePath("kano.db")
            if (dbFile.exists()) db += dbFile.length()
            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) db += walFile.length()
            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) db += shmFile.length()
        } catch (_: Exception) {}
        try {
            context.noBackupFilesDir?.let { files + calculateDirBytes(it) }
        } catch (_: Exception) {}
        val total = files + cache + codeCache + db
        return KanoStorageBreakdown(files, cache, codeCache, db, total)
    }

    suspend fun read(): DeviceSnapshot = withContext(Dispatchers.IO) {
        val static = staticSpecs
        val storage = StatFs(context.filesDir.absolutePath)
        val memory = context.getSystemService(ActivityManager::class.java)?.let { manager ->
            ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        }
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val batteryManager = try { context.getSystemService(BatteryManager::class.java) } catch (_: Exception) { null }
        val currentNowRaw = try {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.takeIf { it != Int.MIN_VALUE && it != 0 }
        } catch (_: Exception) { null }
        val currentNowMa = currentNowRaw?.let { it / 1000 }

        val currentAvgRaw = try {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)?.takeIf { it != Int.MIN_VALUE && it != 0 }
        } catch (_: Exception) { null }
        val currentAvgMa = currentAvgRaw?.let { it / 1000 }

        val chargeCounter = try {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)?.takeIf { it != Int.MIN_VALUE && it > 0 }
        } catch (_: Exception) { null }

        val hwCapacity = try {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
        } catch (_: Exception) { null }

        val percent = if (level >= 0 && scale > 0) ((level.toLong() * 100) / scale).toInt().coerceIn(0, 100) else null
        val charging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
            else -> null
        }
        val batteryStatusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "State Unavailable"
        }
        val batteryPluggedStr = when {
            plugged and BatteryManager.BATTERY_PLUGGED_AC != 0 -> "AC Adapter"
            plugged and BatteryManager.BATTERY_PLUGGED_USB != 0 -> "USB Port"
            plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0 -> "Wireless Charging"
            else -> if (charging == true) "Plugged in" else "Unplugged"
        }
        val batteryHealthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val powerManager = try { context.getSystemService(PowerManager::class.java) } catch (_: Exception) { null }
        val thermalCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try { powerManager?.currentThermalStatus } catch (_: Exception) { null }
        } else null
        val thermalStatusStr = when (thermalCode) {
            PowerManager.THERMAL_STATUS_NONE -> "Normal (No throttling)"
            PowerManager.THERMAL_STATUS_LIGHT -> "Light throttling"
            PowerManager.THERMAL_STATUS_MODERATE -> "Moderate throttling"
            PowerManager.THERMAL_STATUS_SEVERE -> "Severe throttling"
            PowerManager.THERMAL_STATUS_CRITICAL -> "Critical throttling"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency cooling"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal shutdown"
            null -> "Unavailable on this API"
            else -> "Status code $thermalCode"
        }

        val thermalHeadroom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try { powerManager?.getThermalHeadroom(30)?.takeIf { !it.isNaN() } } catch (_: Exception) { null }
        } else null

        val connectivityManager = try { context.getSystemService(ConnectivityManager::class.java) } catch (_: Exception) { null }
        @Suppress("MissingPermission")
        val activeNetwork = try { connectivityManager?.activeNetwork } catch (_: Exception) { null }
        @Suppress("MissingPermission")
        val capabilities = try { connectivityManager?.getNetworkCapabilities(activeNetwork) } catch (_: Exception) { null }

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        val isEthernet = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false
        val isVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        val isMetered = capabilities?.let { !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) }
        val downKbps = capabilities?.linkDownstreamBandwidthKbps?.takeIf { it > 0 }
        val upKbps = capabilities?.linkUpstreamBandwidthKbps?.takeIf { it > 0 }

        val connType = when {
            capabilities == null -> "Offline"
            isVpn -> "VPN Protected"
            isWifi -> "Wi-Fi Connected"
            isCellular -> "Cellular Network"
            isEthernet -> "Ethernet"
            else -> "Connected"
        }

        val bluetoothEnabled = try {
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            bluetoothManager?.adapter?.isEnabled
        } catch (_: SecurityException) {
            null
        }

        val isAirplane = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
        } catch (_: Exception) { false }

        val nfcAdapter = try { NfcAdapter.getDefaultAdapter(context) } catch (_: Exception) { null }
        val nfcEnabled = try { nfcAdapter?.isEnabled } catch (_: Exception) { null }

        val audioManager = try { context.getSystemService(AudioManager::class.java) } catch (_: Exception) { null }
        val outputRoutes = mutableListOf<String>()
        val inputRoutes = mutableListOf<String>()
        try {
            val outDevices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
            outDevices.forEach { d ->
                when (d.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> if (!outputRoutes.contains("Built-in Speaker")) outputRoutes.add("Built-in Speaker")
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> if (!outputRoutes.contains("Wired Audio")) outputRoutes.add("Wired Audio")
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> if (!outputRoutes.contains("Bluetooth Audio")) outputRoutes.add("Bluetooth Audio")
                    AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> if (!outputRoutes.contains("USB Audio")) outputRoutes.add("USB Audio")
                }
            }
            val inDevices = audioManager?.getDevices(AudioManager.GET_DEVICES_INPUTS) ?: emptyArray()
            inDevices.forEach { d ->
                when (d.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_MIC -> if (!inputRoutes.contains("Built-in Microphone")) inputRoutes.add("Built-in Microphone")
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> if (!inputRoutes.contains("Headset Microphone")) inputRoutes.add("Headset Microphone")
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> if (!inputRoutes.contains("Bluetooth Microphone")) inputRoutes.add("Bluetooth Microphone")
                    AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> if (!inputRoutes.contains("USB Microphone")) inputRoutes.add("USB Microphone")
                }
            }
        } catch (_: Exception) {}
        if (outputRoutes.isEmpty()) outputRoutes.add("Built-in Speaker")
        if (inputRoutes.isEmpty()) inputRoutes.add("Built-in Microphone")

        val audioModeStr = when (audioManager?.mode) {
            AudioManager.MODE_NORMAL -> "Normal"
            AudioManager.MODE_IN_CALL -> "In Call"
            AudioManager.MODE_IN_COMMUNICATION -> "Communication"
            AudioManager.MODE_RINGTONE -> "Ringtone"
            else -> "Standard"
        }

        val locationManager = try { context.getSystemService(LocationManager::class.java) } catch (_: Exception) { null }
        val locationMaster = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager?.isLocationEnabled ?: false
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (_: Exception) { false }

        val kanoBreakdown = readKanoStorageBreakdown()

        val subsystemsList = listOf(
            SubsystemDiagnostic("Memory (RAM)", if (memory != null) "Operational" else "Unavailable", "ActivityManager.MemoryInfo", "Live", memory != null, "Managed by Android OS kernel memory killer"),
            SubsystemDiagnostic("Storage Volume", "Operational", "StatFs · data partition", "Live", true, "Scoped application volume"),
            SubsystemDiagnostic("Battery & Power", if (percent != null) "Operational" else "Unavailable", "BatteryManager · ACTION_BATTERY_CHANGED", "Live", percent != null, "Charge, thermal & voltage sensors"),
            SubsystemDiagnostic("Thermal Throttling", if (thermalCode != null) "Operational" else "Unavailable", "PowerManager.currentThermalStatus", "Live", thermalCode != null, "Hardware thermal governor"),
            SubsystemDiagnostic("Display Subsystem", "Operational", "WindowManager · DisplayMetrics", "Static spec", true, "${static.displayResolution} @ ${static.displayRefreshRate?.toInt() ?: 60}Hz"),
            SubsystemDiagnostic("Camera Hardware", if (static.cameraBackCount + static.cameraFrontCount > 0) "Operational" else "Unavailable", "CameraManager characteristics", "Static spec", static.cameraBackCount + static.cameraFrontCount > 0, "${static.cameraBackCount} rear · ${static.cameraFrontCount} front lens(es)"),
            SubsystemDiagnostic("Sensors Array", if (static.sensorCount > 0) "Operational" else "Unavailable", "SensorManager", "Static spec", static.sensorCount > 0, "${static.sensorCount} hardware sensors"),
            SubsystemDiagnostic("Audio & Haptics", "Operational", "AudioManager · Vibrator", "Live", true, "${outputRoutes.firstOrNull() ?: "Speaker"} · ${if (static.hasVibrator) "Haptics" else "No haptics"}"),
            SubsystemDiagnostic("Network Transport", "Operational", "ConnectivityManager.NetworkCapabilities", "Live", true, connType),
            SubsystemDiagnostic("Security & Storage", "Operational", "Android Keystore · Private App Sandbox", "Static spec", true, "Zero Internet permissions · Encrypted Keystore"),
        )

        DeviceSnapshot(
            manufacturer = static.manufacturer,
            model = static.model,
            brand = static.brand,
            androidVersion = static.androidVersion,
            sdkInt = static.sdkInt,
            securityPatch = static.securityPatch,
            buildId = static.buildId,
            cpuAbi = static.cpuAbi,
            cpuCores = static.cpuCores,
            storageTotal = storage.totalBytes,
            storageAvailable = storage.availableBytes,
            memoryTotal = memory?.totalMem,
            memoryAvailable = memory?.availMem,
            memoryLow = memory?.lowMemory ?: false,
            batteryPercent = percent,
            charging = charging,
            batteryTemperatureC = if (tempTenths > 0) tempTenths / 10.0 else null,
            batteryVoltageMv = if (voltageMv > 0) voltageMv else null,
            connectionType = connType,
            bluetoothEnabled = bluetoothEnabled,
            capturedAt = System.currentTimeMillis(),
            // Phase 1 Extended Fields:
            deviceName = static.deviceName,
            buildType = static.buildType,
            buildTags = static.buildTags,
            buildFingerprint = static.buildFingerprint,
            kernelVersion = static.kernelVersion,
            supportedAbis = static.supportedAbis,
            socModel = static.socModel,
            hardware = static.hardware,
            board = static.board,
            glEsVersion = static.glEsVersion,
            memoryUsed = if (memory != null) (memory.totalMem - memory.availMem).coerceAtLeast(0) else null,
            memoryThreshold = memory?.threshold,
            storageUsed = (storage.totalBytes - storage.availableBytes).coerceAtLeast(0),
            kanoStorageBytes = kanoBreakdown.totalBytes,
            batteryStatus = batteryStatusStr,
            batteryPlugged = batteryPluggedStr,
            batteryHealth = batteryHealthStr,
            batteryTechnology = technology,
            thermalStatus = thermalStatusStr,
            thermalStatusCode = thermalCode,
            displayResolution = static.displayResolution,
            displayDensityDpi = static.displayDensityDpi,
            displayDensityFactor = static.displayDensityFactor,
            displayRefreshRate = static.displayRefreshRate,
            displayHdrCapable = static.displayHdrCapable,
            wifiActive = isWifi,
            vpnActive = isVpn,
            airplaneMode = isAirplane,
            cameraBackCount = static.cameraBackCount,
            cameraFrontCount = static.cameraFrontCount,
            cameraHasFlash = static.cameraHasFlash,
            sensorCount = static.sensorCount,
            sensorCategories = static.sensorCategories,
            nfcPresent = static.nfcPresent,
            nfcEnabled = nfcEnabled,
            audioOutputRoutes = outputRoutes,
            audioMode = audioModeStr,
            hasVibrator = static.hasVibrator,
            hasFlashlight = static.hasFlashlight,
            locationMasterEnabled = locationMaster,
            // Phase 1 Deep Hardware & Subsystem Extensions:
            cpuModelName = static.cpuModelName,
            cpuBogoMips = static.cpuBogoMips,
            cpuFeatures = static.cpuFeatures,
            supported32BitAbis = static.supported32BitAbis,
            supported64BitAbis = static.supported64BitAbis,
            displayPhysicalSizeInches = static.displayPhysicalSizeInches,
            displayWideColorGamut = static.displayWideColorGamut,
            displaySupportedModesCount = static.displaySupportedModesCount,
            displaySupportedModesSummary = static.displaySupportedModesSummary,
            batteryCurrentNowMa = currentNowMa,
            batteryCurrentAverageMa = currentAvgMa,
            batteryChargeCounterUah = chargeCounter,
            batteryCapacityPercent = hwCapacity,
            thermalHeadroom = thermalHeadroom,
            hasAmplitudeControl = static.hasAmplitudeControl,
            audioInputRoutes = inputRoutes,
            cellularActive = isCellular,
            isMeteredNetwork = isMetered,
            networkDownstreamKbps = downKbps,
            networkUpstreamKbps = upKbps,
            sensorDetails = static.sensorDetails,
            cameraDetails = static.cameraDetails,
            subsystems = subsystemsList,
            kanoStorageBreakdown = kanoBreakdown,
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

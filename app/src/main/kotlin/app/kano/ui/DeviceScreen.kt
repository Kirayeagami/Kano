package app.kano.ui

import android.content.Intent
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs

@Composable
fun DeviceScreen(
    state: DeviceState,
    refresh: () -> Unit,
    model: KanoViewModel? = null,
    openMedia: () -> Unit = {},
    initialPanel: String = "Overview",
    footerInset: Dp = 0.dp,
    openStorage: () -> Unit = {},
) {
    val context = LocalContext.current
    var panel by rememberSaveable { mutableStateOf(initialPanel) }
    LaunchedEffect(initialPanel) { if (initialPanel != "Overview") panel = initialPanel }
    var actionError by remember { mutableStateOf<String?>(null) }
    val listState = remember(panel) { LazyListState() }
    BackHandler(panel != "Overview") { panel = "Overview" }

    fun bytes(value: Long?) = value?.let { Formatter.formatFileSize(context, it) } ?: "Unavailable"
    fun settings(action: String) {
        try {
            context.startActivity(Intent(action))
            actionError = null
        } catch (_: Exception) {
            actionError = "This Android settings page is unavailable on this device."
        }
    }

    val snapshot = (state as? DeviceState.Ready)?.snapshot
    val colors = KanoThemeColors

    val navSections = listOf(
        "Overview",
        "Performance",
        "RAM",
        "Storage",
        "Battery",
        "Thermal",
        "Connectivity",
        "Hardware",
        "Sensors",
        "Diagnostics"
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 20.dp + footerInset
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                if (panel == "Overview") "Device Intelligence" else panel,
                if (snapshot == null) "Official Android readings" else "${snapshot.manufacturer} ${snapshot.model} · Android ${snapshot.androidVersion}"
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                navSections.forEach { sectionName ->
                    val isSelected = panel == sectionName
                    FilterChip(
                        selected = isSelected,
                        onClick = { panel = sectionName },
                        label = { Text(sectionName, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent.copy(alpha = 0.18f),
                            selectedLabelColor = colors.accent,
                            containerColor = colors.surface.copy(alpha = 0.5f),
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) colors.accent else colors.divider
                        )
                    )
                }
            }
        }

        when (state) {
            DeviceState.Loading -> item {
                KanoStateSurface("Reading your device", "Refreshing Android system readings.", true)
            }
            DeviceState.Failed -> item {
                KanoStateSurface("Readings unavailable", "Could not read system metrics.")
                Spacer(Modifier.height(8.dp))
                KanoOutlinedButton(refresh) { Text("Retry Readings") }
            }
            is DeviceState.Ready -> {
                val data = state.snapshot
                val used = (data.storageTotal - data.storageAvailable).coerceAtLeast(0)
                val percent = if (data.storageTotal > 0) (used * 100.0 / data.storageTotal).toInt() else null

                if (panel == "Overview") {
                    item {
                        KanoGlassCard {
                            Text("Your phone, now", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                            val titleText = if (data.deviceName.equals(data.model, ignoreCase = true)) {
                                data.model
                            } else {
                                "${data.deviceName} · ${data.model}"
                            }
                            Text(titleText, style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
                            Text(
                                "Android ${data.androidVersion} (API ${data.sdkInt}) · Patch ${data.securityPatch}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                "Data volume · " + DateFormat.getTimeInstance().format(Date(data.capturedAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary
                            )
                        }
                    }

                    item {
                        Text("System Resources", style = MaterialTheme.typography.titleSmall, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(4.dp))
                        KanoCardGroup {
                            KanoGroupItem("Storage", "${percent ?: "Unavailable"}% used · ${bytes(data.storageTotal)} volume", { panel = "Storage" }, showDivider = true)
                            KanoGroupItem("RAM", "${bytes(data.memoryAvailable)} available · Android managed", { panel = "RAM" }, showDivider = true)
                            KanoGroupItem("Battery", (data.batteryPercent?.let { "$it% · " } ?: "") + data.batteryStatus, { panel = "Battery" }, showDivider = true)
                            KanoGroupItem("Thermal", data.thermalStatus, { panel = "Thermal" }, showDivider = false)
                        }
                    }

                    item {
                        Text("Hardware & Platform", style = MaterialTheme.typography.titleSmall, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(4.dp))
                        KanoCardGroup {
                            KanoGroupItem("Performance", "${data.socModel ?: data.cpuModelName ?: data.hardware} · ${data.cpuCores} cores", { panel = "Performance" }, showDivider = true)
                            KanoGroupItem("Hardware", "${data.displayResolution} · ${data.cameraBackCount} rear · ${data.cameraFrontCount} front", { panel = "Hardware" }, showDivider = true)
                            KanoGroupItem("Sensors", "${data.sensorCount} hardware sensors", { panel = "Sensors" }, showDivider = true)
                            KanoGroupItem("Connectivity", data.connectionType, { panel = "Connectivity" }, showDivider = true)
                            KanoGroupItem("Diagnostics", "${data.subsystems.count { it.isOperational }} of ${data.subsystems.size} subsystems operational", { panel = "Diagnostics" }, showDivider = true)
                            KanoGroupItem("System Identity", "Android ${data.androidVersion} · ${data.cpuAbi}", { panel = "System" }, showDivider = true)
                            KanoGroupItem("Security", "Patch ${data.securityPatch} · 0 Internet permissions", { panel = "Security" }, showDivider = false)
                        }
                    }
                } else {
                    item {
                        KanoGlassCard {
                            when (panel) {
                                "Performance", "CPU" -> {
                                    Text(data.socModel ?: data.cpuModelName ?: data.hardware, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("SoC / Chipset model", data.socModel ?: "Not reported by OEM", "Build.SOC_MODEL (API 31+)", "Static spec", status = if (data.socModel != null) "Available" else "Unavailable")
                                    MetricRow("CPU processor model", data.cpuModelName ?: data.hardware, "/proc/cpuinfo · model name", "Static spec", status = if (data.cpuModelName != null) "Available" else "Unavailable")
                                    MetricRow("Hardware name", data.hardware, "Build.HARDWARE", "Static spec")
                                    MetricRow("Motherboard platform", data.board, "Build.BOARD", "Static spec")
                                    MetricRow("Primary CPU architecture", data.cpuAbi, "Build.SUPPORTED_ABIS[0]", "Static spec")
                                    MetricRow("Supported 64-bit ABIs", data.supported64BitAbis.joinToString(", ").ifBlank { "None" }, "Build.SUPPORTED_64_BIT_ABIS", "Static spec")
                                    MetricRow("Supported 32-bit ABIs", data.supported32BitAbis.joinToString(", ").ifBlank { "None (64-bit only architecture)" }, "Build.SUPPORTED_32_BIT_ABIS", "Static spec")
                                    MetricRow("Available CPU cores", "${data.cpuCores} logical cores", "Runtime.availableProcessors", "Static spec", note = "Reports available logical processor cores; not real-time CPU utilization")
                                    MetricRow("Instruction features", data.cpuFeatures.joinToString(", ").ifBlank { "Standard 64-bit instruction sets" }, "/proc/cpuinfo · flags", "Static spec")
                                    MetricRow("BogoMIPS rating", data.cpuBogoMips?.let { "$it BogoMIPS" } ?: "Unavailable", "/proc/cpuinfo · bogomips", "Static spec", status = if (data.cpuBogoMips != null) "Available" else "Unavailable")
                                    MetricRow("GPU / OpenGL ES version", data.glEsVersion ?: "Unavailable", "ActivityManager · deviceConfigurationInfo", "Static spec")
                                    MetricRow("Core clock frequencies", "Unavailable on this device", "Kernel cpuinfo_cur_freq", "Unavailable", status = "Unavailable", note = "Live core clock frequencies are restricted for security on public Android releases.")
                                }
                                "Storage" -> {
                                    Text(percent?.let { "$it% used" } ?: "Capacity unavailable", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
                                    val targetProgress = ((percent ?: 0) / 100f).coerceIn(0f, 1f)
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = targetProgress,
                                        animationSpec = if (LocalMotionEnabled.current) tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.Emphasized) else snap(),
                                        label = "storageProgress"
                                    )
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = colors.accent,
                                        trackColor = colors.divider
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    MetricRow("Volume capacity", bytes(data.storageTotal), "StatFs · data volume", "Static spec")
                                    MetricRow("Used storage", bytes(used), "Calculated: Total - Available", "Live")
                                    MetricRow("Available storage", bytes(data.storageAvailable), "StatFs · availableBytes", "Live")
                                    MetricRow("Kano app storage total", bytes(data.kanoStorageBytes), "App-private data & cache directory", "Live", note = "Includes SQLite database, preferences, and thumbnail cache.")
                                    MetricRow("• App private files", bytes(data.kanoStorageBreakdown.internalFilesBytes), "Context.filesDir", "Live")
                                    MetricRow("• App internal cache", bytes(data.kanoStorageBreakdown.internalCacheBytes), "Context.cacheDir", "Live")
                                    MetricRow("• App code cache", bytes(data.kanoStorageBreakdown.codeCacheBytes), "Context.codeCacheDir", "Live")
                                    MetricRow("• Local database", bytes(data.kanoStorageBreakdown.databaseBytes), "Room SQLite database files", "Live")
                                    Text(
                                        "Source: StatFs on Kano's data volume. System partitions, other apps' caches and all-file totals cannot be measured without all-files access.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    KanoCardGroup {
                                        KanoGroupItem("Storage intelligence", "Index authorized media and files; review duplicates and large items", openStorage, showDivider = true)
                                        KanoGroupItem("Photos & videos", "Review permitted media, real sizes and large files", openMedia, showDivider = true)
                                        KanoGroupItem("Android storage explorer", "System-managed categories and cleanup", { settings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }, showDivider = false)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    FactRow("Cleanup", "Unavailable in Kano", "Source deletion is withheld until durable extraction evidence and OS confirmation are implemented.")
                                }
                                "RAM" -> {
                                    Text(bytes(data.memoryAvailable) + " available", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Available RAM", bytes(data.memoryAvailable), "ActivityManager.MemoryInfo · availMem", "Live")
                                    MetricRow("Total RAM", bytes(data.memoryTotal), "ActivityManager.MemoryInfo · totalMem", "Static spec")
                                    MetricRow("Used RAM", bytes(data.memoryUsed), "Calculated: totalMem - availMem", "Live")
                                    MetricRow("Low-memory pressure", if (data.memoryTotal == null) "Unavailable" else if (data.memoryLow) "Android reports low memory" else "Normal memory headroom", "ActivityManager.MemoryInfo · lowMemory", "Live", status = if (data.memoryLow) "Low" else "Normal")
                                    MetricRow("Low-memory threshold", bytes(data.memoryThreshold), "ActivityManager.MemoryInfo · threshold", "Static spec", note = "Threshold below which Android starts terminating background processes.")
                                    MetricRow("RAM utilization", if (data.memoryTotal != null && data.memoryUsed != null && data.memoryTotal > 0) "${((data.memoryUsed * 100.0) / data.memoryTotal).toInt()}% in use" else "Unavailable", "Calculated: memoryUsed / totalMem", "Live")
                                    Text(
                                        "Optimize RAM drops Kano's thumbnail-cache references only. Other apps and Android's memory cache are managed by the OS.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    val optimizing = model?.memoryOptimizing?.collectAsState()?.value ?: false
                                    KanoHeroButton(onClick = { model?.optimizeMemory() }, enabled = model != null && !optimizing) {
                                        Text(if (optimizing) "Measuring…" else "Optimize RAM")
                                    }
                                    val report = model?.memoryReport?.collectAsState()?.value
                                    report?.let {
                                        Spacer(Modifier.height(8.dp))
                                        FactRow("RAM before", bytes(it.beforeAvailable))
                                        FactRow("Action", "Cleared Kano thumbnail cache")
                                        FactRow("Cache references dropped", bytes(it.cacheBefore - it.cacheAfter), "Bitmaps still displayed by the UI can remain in memory.")
                                        FactRow("RAM after", bytes(it.afterAvailable))
                                        Text("Measured " + DateFormat.getTimeInstance().format(Date(it.capturedAt)), style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                                        Text("System RAM varies independently. These readings do not prove a performance gain.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                    }
                                }
                                "Battery" -> {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(data.batteryPercent?.let { "$it%" } ?: "Unavailable", style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
                                        StatusChip(data.batteryStatus)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Charge level", data.batteryPercent?.let { "$it%" } ?: "Unavailable", "BatteryManager · EXTRA_LEVEL", "Live")
                                    MetricRow("Charging state", data.batteryStatus, "BatteryManager · EXTRA_STATUS", "Live")
                                    MetricRow("Power source", data.batteryPlugged, "BatteryManager · EXTRA_PLUGGED", "Live")
                                    MetricRow("Battery health", data.batteryHealth, "BatteryManager · EXTRA_HEALTH", "Live", status = if (data.batteryHealth == "Good") "Good" else "Warning")
                                    MetricRow("Battery temperature", data.batteryTemperatureC?.let { "$it °C" } ?: "Unavailable", "BatteryManager · EXTRA_TEMPERATURE", "Live")
                                    MetricRow("Battery voltage", data.batteryVoltageMv?.let { "$it mV (${"%.2f".format(it / 1000.0)} V)" } ?: "Unavailable", "BatteryManager · EXTRA_VOLTAGE", "Live")
                                    MetricRow("Instantaneous current", data.batteryCurrentNowMa?.let { "${abs(it)} mA (${if (it < 0 || data.charging != true) "discharge rate" else "charge rate"})" } ?: "Unavailable on this device", "BatteryManager · BATTERY_PROPERTY_CURRENT_NOW", "Live", status = if (data.batteryCurrentNowMa != null) "Available" else "Unavailable")
                                    MetricRow("Average current", data.batteryCurrentAverageMa?.let { "${abs(it)} mA" } ?: "Unavailable on this device", "BatteryManager · BATTERY_PROPERTY_CURRENT_AVERAGE", "Live", status = if (data.batteryCurrentAverageMa != null) "Available" else "Unavailable")
                                    MetricRow("Remaining charge counter", data.batteryChargeCounterUah?.let { "${it / 1000} mAh (${it} µAh)" } ?: "Unavailable on this device", "BatteryManager · BATTERY_PROPERTY_CHARGE_COUNTER", "Live", status = if (data.batteryChargeCounterUah != null) "Available" else "Unavailable")
                                    MetricRow("Hardware capacity gauge", data.batteryCapacityPercent?.let { "$it%" } ?: "Unavailable on this device", "BatteryManager · BATTERY_PROPERTY_CAPACITY", "Live", status = if (data.batteryCapacityPercent != null) "Available" else "Unavailable")
                                    MetricRow("Battery technology", data.batteryTechnology ?: "Li-ion", "BatteryManager · EXTRA_TECHNOLOGY", "Static spec")
                                    MetricRow("Capacity health / cycle count", "Unavailable on this device", "OEM / Android API", "Static spec", status = "Unavailable", note = "Cycle count is restricted to system-privileged apps on public Android releases.")
                                    Spacer(Modifier.height(8.dp))
                                    KanoOutlinedButton({ settings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }) { Text("Android Battery Settings") }
                                }
                                "Thermal" -> {
                                    Text(data.thermalStatus, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Thermal status", data.thermalStatus, "PowerManager · currentThermalStatus", "Live", status = if (data.thermalStatusCode == 0) "Normal" else "Throttling", note = "Indicates whether Android is throttling CPU/GPU clock speeds to cool down.")
                                    MetricRow("Thermal status code", data.thermalStatusCode?.toString() ?: "Unavailable", "PowerManager · API 29+", "Live")
                                    MetricRow("Thermal headroom forecast", data.thermalHeadroom?.let { "%.2f (headroom ratio to throttling)".format(it) } ?: "Unavailable on this API", "PowerManager · getThermalHeadroom(30)", "Live", status = if (data.thermalHeadroom != null) "Available" else "Unavailable")
                                    MetricRow("Battery temperature", data.batteryTemperatureC?.let { "$it °C" } ?: "Unavailable", "BatteryManager · EXTRA_TEMPERATURE", "Live")
                                    MetricRow("Per-zone hardware sensors", "Unavailable on this device", "Kernel sysfs thermal_zone", "Unavailable", status = "Unavailable", note = "Direct hardware thermal zone access is protected by Android SELinux sandboxing.")
                                }
                                "Connectivity" -> {
                                    Text(data.connectionType, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Active network connection", data.connectionType, "ConnectivityManager · NetworkCapabilities", "Live")
                                    MetricRow("Wi-Fi transport", if (data.wifiActive) "Connected" else "Inactive / Disconnected", "NetworkCapabilities · TRANSPORT_WIFI", "Live")
                                    MetricRow("Cellular transport", if (data.cellularActive) "Connected" else "Inactive / Disconnected", "NetworkCapabilities · TRANSPORT_CELLULAR", "Live")
                                    MetricRow("Network metering", data.isMeteredNetwork?.let { if (it) "Metered connection" else "Unmetered connection" } ?: "Unavailable", "NetworkCapabilities · NET_CAPABILITY_NOT_METERED", "Live")
                                    MetricRow("Downstream bandwidth", data.networkDownstreamKbps?.let { if (it >= 1000) "${it / 1000} Mbps" else "$it Kbps" } ?: "Unavailable", "NetworkCapabilities · linkDownstreamBandwidthKbps", "Live", status = if (data.networkDownstreamKbps != null) "Available" else "Unavailable")
                                    MetricRow("Upstream bandwidth", data.networkUpstreamKbps?.let { if (it >= 1000) "${it / 1000} Mbps" else "$it Kbps" } ?: "Unavailable", "NetworkCapabilities · linkUpstreamBandwidthKbps", "Live", status = if (data.networkUpstreamKbps != null) "Available" else "Unavailable")
                                    MetricRow("Bluetooth adapter", data.bluetoothEnabled?.let { if (it) "Enabled" else "Disabled" } ?: "Permission Required (Local Only)", "BluetoothManager · adapter.isEnabled", "Live", status = if (data.bluetoothEnabled != null) "Available" else "Permission Required")
                                    MetricRow("VPN protection", if (data.vpnActive) "VPN Active" else "No VPN Active", "NetworkCapabilities · TRANSPORT_VPN", "Live")
                                    MetricRow("Airplane mode", if (data.airplaneMode) "Enabled" else "Disabled", "Settings.Global · AIRPLANE_MODE_ON", "Live")
                                    Spacer(Modifier.height(8.dp))
                                    KanoOutlinedButton({ settings(Settings.ACTION_WIRELESS_SETTINGS) }) { Text("Android connection settings") }
                                }
                                "Hardware" -> {
                                    Text("Peripherals & Hardware", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    KanoCardGroup {
                                        KanoGroupItem("Display", "${data.displayResolution} · ${data.displayRefreshRate?.let { "%.0f Hz".format(it) } ?: "Standard"}", { panel = "Display" }, showDivider = true)
                                        KanoGroupItem("Camera", "${data.cameraBackCount} rear · ${data.cameraFrontCount} front lenses", { panel = "Camera" }, showDivider = true)
                                        KanoGroupItem("Audio & Haptics", "${data.audioOutputRoutes.firstOrNull() ?: "Speaker"} · ${if (data.hasVibrator) "Haptics" else "No haptics"}", { panel = "Audio" }, showDivider = false)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    MetricRow("Display screen", "${data.displayResolution} · ${data.displayDensityDpi} dpi", "WindowManager / DisplayMetrics", "Static spec")
                                    MetricRow("Camera modules", "${data.cameraBackCount} rear · ${data.cameraFrontCount} front", "CameraManager", "Static spec")
                                    MetricRow("Haptic vibration motor", if (data.hasVibrator) "Available" else "Unavailable", "VibratorManager / Vibrator", "Static spec")
                                    MetricRow("Haptic amplitude control", if (data.hasAmplitudeControl) "Supported (Fine-grained haptics)" else "Unsupported (Binary rumble only)", "Vibrator · hasAmplitudeControl()", "Static spec", status = if (data.hasAmplitudeControl) "Available" else "Unavailable")
                                    MetricRow("NFC hardware", if (data.nfcPresent) (if (data.nfcEnabled == true) "Present & Enabled" else "Present & Disabled") else "Not Present on Device", "NfcAdapter", "Live", status = if (data.nfcPresent) "Available" else "Hardware Absent")
                                    MetricRow("Flashlight / flash unit", if (data.hasFlashlight || data.cameraHasFlash) "Available" else "Unavailable", "PackageManager.FEATURE_CAMERA_FLASH", "Static spec")
                                    MetricRow("Location master switch", if (data.locationMasterEnabled) "Location Services Enabled" else "Location Services Disabled", "LocationManager · isLocationEnabled", "Live", note = "Reports device master switch state only; zero GPS tracking or coordinates queried.")
                                }
                                "Display" -> {
                                    Text(data.displayResolution, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Screen resolution", data.displayResolution, "WindowManager · WindowMetrics", "Static spec")
                                    MetricRow("Screen density", "${data.displayDensityDpi} dpi", "DisplayMetrics · densityDpi", "Static spec")
                                    MetricRow("Density scale factor", "${data.displayDensityFactor}x", "DisplayMetrics · density", "Static spec")
                                    MetricRow("Physical screen diagonal", data.displayPhysicalSizeInches?.let { "%.1f inches".format(it) } ?: "Unavailable", "Calculated: xdpi / ydpi", "Static spec", status = if (data.displayPhysicalSizeInches != null) "Available" else "Unavailable")
                                    MetricRow("Refresh rate", data.displayRefreshRate?.let { "%.1f Hz".format(it) } ?: "Unavailable", "Display · refreshRate", "Live")
                                    MetricRow("Supported display modes", data.displaySupportedModesSummary, "Display · supportedModes", "Static spec")
                                    MetricRow("High Dynamic Range (HDR)", data.displayHdrCapable?.let { if (it) "Supported" else "Standard Dynamic Range (SDR)" } ?: "Unavailable", "Display · isHdr", "Static spec")
                                    MetricRow("Wide color gamut (P3)", data.displayWideColorGamut?.let { if (it) "Supported (Wide Color Gamut)" else "Standard sRGB" } ?: "Unavailable", "Display · isWideColorGamut", "Static spec")
                                }
                                "Camera" -> {
                                    Text("${data.cameraBackCount} Rear · ${data.cameraFrontCount} Front", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Rear camera lenses", "${data.cameraBackCount} lens module(s)", "CameraManager · LENS_FACING_BACK", "Static spec")
                                    MetricRow("Front selfie lenses", "${data.cameraFrontCount} lens module(s)", "CameraManager · LENS_FACING_FRONT", "Static spec")
                                    MetricRow("Flashlight / flash unit", if (data.cameraHasFlash || data.hasFlashlight) "Available" else "Unavailable", "CameraCharacteristics · FLASH_INFO_AVAILABLE", "Static spec")
                                    if (data.cameraDetails.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text("Camera Lens Details", style = MaterialTheme.typography.titleSmall, color = colors.textSecondary)
                                        Spacer(Modifier.height(4.dp))
                                        data.cameraDetails.forEach { cam ->
                                            FactRow("Camera #${cam.id} (${cam.facing})", cam.maxResolution ?: "Resolution not reported", "HW Level: ${cam.hardwareLevel} · ${cam.orientationDegrees}° orientation · ${if (cam.hasFlash) "Flash" else "No flash"}")
                                        }
                                    }
                                    MetricRow("Inspection mode", "Hardware characteristics only", "CameraManager API", "Static spec", note = "Queried without activating the camera preview or recording video.")
                                }
                                "Audio" -> {
                                    Text(data.audioOutputRoutes.firstOrNull() ?: "Speaker", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Connected audio outputs", data.audioOutputRoutes.joinToString(", "), "AudioManager · getDevices(OUTPUTS)", "Live")
                                    MetricRow("Audio input microphones", data.audioInputRoutes.joinToString(", "), "AudioManager · getDevices(INPUTS)", "Live")
                                    MetricRow("Audio stream mode", data.audioMode, "AudioManager · mode", "Live")
                                    MetricRow("Haptic vibration motor", if (data.hasVibrator) "Available" else "Unavailable", "VibratorManager / Vibrator", "Static spec")
                                    MetricRow("Haptic amplitude control", if (data.hasAmplitudeControl) "Supported (Fine-grained haptics)" else "Unsupported (Binary rumble only)", "Vibrator · hasAmplitudeControl()", "Static spec", status = if (data.hasAmplitudeControl) "Available" else "Unavailable")
                                    MetricRow("Camera flashlight unit", if (data.hasFlashlight) "Available" else "Unavailable", "PackageManager.FEATURE_CAMERA_FLASH", "Static spec")
                                    MetricRow("Volume telemetry", "Managed by Android", "AudioManager", "Static spec", note = "Volume levels are controlled directly through physical device volume buttons.")
                                }
                                "Sensors" -> {
                                    Text("${data.sensorCount} Hardware Sensors", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Total hardware sensors", data.sensorCount.toString(), "SensorManager · getSensorList(TYPE_ALL)", "Static spec")
                                    MetricRow("Primary sensor categories", data.sensorCategories.joinToString(", ").ifBlank { "Standard motion and environmental sensors" }, "SensorManager inventory", "Static spec")
                                    if (data.sensorDetails.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text("Top Hardware Sensors", style = MaterialTheme.typography.titleSmall, color = colors.textSecondary)
                                        Spacer(Modifier.height(4.dp))
                                        data.sensorDetails.take(15).forEach { sensor ->
                                            FactRow(sensor.name, "${sensor.vendor} · ${sensor.typeString}", "Power: ${sensor.powerMa} mA · Res: ${sensor.resolution}")
                                        }
                                    }
                                    MetricRow("Sensor telemetry", "Local inventory only", "Kano privacy boundary", "Static spec", note = "Kano queries sensor inventory only; no sensor listener or background data collection is active.")
                                }
                                "Diagnostics" -> {
                                    Text("Subsystem Health & Diagnostics", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Subsystem Verification", style = MaterialTheme.typography.titleSmall, color = colors.textSecondary)
                                    Spacer(Modifier.height(4.dp))
                                    data.subsystems.forEach { diag ->
                                        FactRow("${diag.name}: ${diag.status}", diag.details, "Source: ${diag.source} · ${diag.freshness}")
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Location master switch", if (data.locationMasterEnabled) "Location Services Enabled" else "Location Services Disabled", "LocationManager · isLocationEnabled", "Live", note = "Reports device master switch state only; zero GPS tracking or coordinates queried.")
                                    MetricRow("Benchmark / Health score", "Unavailable", "Kano policy", "Unavailable", status = "Unavailable", note = "No fabricated benchmark score, artificial grade or simulated device health percentage.")
                                    MetricRow("Data truth guarantee", "100% Genuine", "Android public APIs", "Live", status = "Available", note = "All metrics are queried directly from Android OS services. Zero synthetic or estimated readings.")
                                }
                                "System" -> {
                                    Text("${data.brand} ${data.model}", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Manufacturer", data.manufacturer, "Build.MANUFACTURER", "Static spec")
                                    MetricRow("Brand", data.brand, "Build.BRAND", "Static spec")
                                    MetricRow("Model", data.model, "Build.MODEL", "Static spec")
                                    MetricRow("Device name", data.deviceName, "Settings.Global.DEVICE_NAME", "Static spec")
                                    MetricRow("Android version", "${data.androidVersion} (API ${data.sdkInt})", "Build.VERSION", "Static spec")
                                    MetricRow("Security patch level", data.securityPatch, "Build.VERSION.SECURITY_PATCH", "Static spec")
                                    MetricRow("Build display ID", data.buildId, "Build.DISPLAY", "Static spec")
                                    MetricRow("Build type & tags", "${data.buildType} · ${data.buildTags}", "Build.TYPE / Build.TAGS", "Static spec")
                                    MetricRow("Build fingerprint", data.buildFingerprint, "Build.FINGERPRINT", "Static spec")
                                    MetricRow("Kernel & OS architecture", data.kernelVersion, "System os.version & os.arch", "Static spec")
                                    MetricRow("Primary CPU ABI", data.cpuAbi, "Build.SUPPORTED_ABIS[0]", "Static spec")
                                    MetricRow("Logical CPU cores", "${data.cpuCores} cores", "Runtime.availableProcessors", "Static spec", note = "Reports available logical cores; not real-time CPU utilization")
                                }
                                "Security" -> {
                                    Text("Security & Privacy Model", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    MetricRow("Security patch level", data.securityPatch, "Build.VERSION.SECURITY_PATCH", "Static spec")
                                    MetricRow("Kano data storage", "App private · backups disabled", "Room database · Keystore AES-256-GCM", "Static spec", note = "Database-level encryption is not implemented. Credential storage uses Android Keystore.")
                                    MetricRow("Cloud network boundary", "Disabled", "Zero Internet permission", "Static spec", note = "No Internet permission or live account/provider.")
                                    MetricRow("Platform sandboxing", "SELinux Enforcing", "Android Application Sandbox", "Static spec")
                                    Spacer(Modifier.height(8.dp))
                                    KanoOutlinedButton({ settings(Settings.ACTION_SECURITY_SETTINGS) }) { Text("Android security settings") }
                                }
                                else -> {
                                    MetricRow("Manufacturer", data.manufacturer, "Build.MANUFACTURER", "Static spec")
                                    MetricRow("Model", data.model, "Build.MODEL", "Static spec")
                                    MetricRow("Android version", "${data.androidVersion} (API ${data.sdkInt})", "Build.VERSION", "Static spec")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Updated " + DateFormat.getTimeInstance().format(Date(data.capturedAt)) + " · sampled every 3 seconds while Kano is visible.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    KanoOutlinedButton(refresh, Modifier.fillMaxWidth()) { Text("Refresh Device Readings") }
                    actionError?.let { Text(it, color = colors.error) }
                }
            }
        }
    }
}

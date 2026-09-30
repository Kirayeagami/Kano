package app.kano

import app.kano.platform.*
import org.junit.Assert.*
import org.junit.Test

class DeviceSnapshotTest {

    @Test
    fun calculatedMemoryUsed_isCorrect() {
        val snapshot = DeviceSnapshot(
            manufacturer = "OnePlus",
            model = "CPH2715",
            brand = "OnePlus",
            androidVersion = "16",
            sdkInt = 36,
            securityPatch = "2026-09-01",
            buildId = "CPH2715_16.0.0.100",
            cpuAbi = "arm64-v8a",
            cpuCores = 8,
            storageTotal = 256_000_000_000L,
            storageAvailable = 150_000_000_000L,
            memoryTotal = 8_000_000_000L,
            memoryAvailable = 3_500_000_000L,
            memoryLow = false,
            batteryPercent = 88,
            charging = false,
            batteryTemperatureC = 28.5,
            batteryVoltageMv = 4120,
            connectionType = "Wi-Fi Connected",
            bluetoothEnabled = true,
            capturedAt = 1790800000000L,
        )

        assertEquals(4_500_000_000L, snapshot.memoryUsed)
        assertEquals(106_000_000_000L, snapshot.storageUsed)
        assertFalse(snapshot.memoryLow)
        assertEquals("OnePlus", snapshot.manufacturer)
        assertEquals("CPH2715", snapshot.model)
        assertEquals(36, snapshot.sdkInt)
    }

    @Test
    fun nullMemory_yieldsNullMemoryUsed() {
        val snapshot = DeviceSnapshot(
            manufacturer = "Generic",
            model = "Device",
            brand = "Brand",
            androidVersion = "15",
            sdkInt = 35,
            securityPatch = "2026-08-01",
            buildId = "BUILD1",
            cpuAbi = "arm64-v8a",
            cpuCores = 4,
            storageTotal = 64_000_000_000L,
            storageAvailable = 20_000_000_000L,
            memoryTotal = null,
            memoryAvailable = null,
            memoryLow = false,
            batteryPercent = null,
            charging = null,
            batteryTemperatureC = null,
            batteryVoltageMv = null,
            connectionType = "Offline",
            bluetoothEnabled = null,
            capturedAt = 1790800000000L,
        )

        assertNull(snapshot.memoryTotal)
        assertNull(snapshot.memoryUsed)
        assertEquals(44_000_000_000L, snapshot.storageUsed)
    }

    @Test
    fun subsystemDiagnostics_verifyCompleteness() {
        val diag = SubsystemDiagnostic(
            name = "Thermal Management",
            status = "Operational",
            source = "PowerManager",
            freshness = "Live",
            isOperational = true,
            details = "Normal (No throttling)"
        )

        assertEquals("Thermal Management", diag.name)
        assertTrue(diag.isOperational)
        assertEquals("Live", diag.freshness)
    }

    @Test
    fun sensorAndCameraDetails_holdTruthfulHardwareData() {
        val sensor = SensorDetail(
            name = "BMA253 Accelerometer",
            vendor = "Bosch Sensortec",
            typeString = "android.sensor.accelerometer",
            powerMa = 0.13f,
            resolution = 0.0039f,
            maxRange = 156.96f
        )
        assertEquals("BMA253 Accelerometer", sensor.name)
        assertEquals("Bosch Sensortec", sensor.vendor)

        val camera = CameraDetail(
            id = "0",
            facing = "Rear (Main)",
            hardwareLevel = "Level 3",
            orientationDegrees = 90,
            hasFlash = true,
            maxResolution = "4000 × 3000 px"
        )
        assertEquals("0", camera.id)
        assertTrue(camera.hasFlash)
        assertEquals(90, camera.orientationDegrees)
    }
}

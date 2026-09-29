package app.kano.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.kano.platform.DeviceSnapshot

// --- Home Screen Previews ---
@Preview(name = "Home Screen - Light", showBackground = true, widthDp = 375)
@Composable
fun HomeScreenPreviewLight() {
    KanoTheme(themeMode = KanoThemeMode.LIGHT) {
        HomeScreen(openDevice = {}, openMedia = {}, openStyle = {}, openPersonalCare = {})
    }
}

@Preview(name = "Home Screen - Dark", showBackground = true, widthDp = 375)
@Composable
fun HomeScreenPreviewDark() {
    KanoTheme(themeMode = KanoThemeMode.DARK) {
        HomeScreen(openDevice = {}, openMedia = {}, openStyle = {}, openPersonalCare = {})
    }
}

// --- Device Screen Previews ---
@Preview(name = "Device Screen - Light", showBackground = true, widthDp = 375)
@Composable
fun DeviceScreenPreviewLight() {
    KanoTheme(themeMode = KanoThemeMode.LIGHT) {
        DeviceScreen(
            state = DeviceState.Ready(
                DeviceSnapshot(
                    model = "Google Pixel 8 Pro (Emulator)",
                    androidVersion = "Android 15 (API 35)",
                    storageTotal = 128L * 1024 * 1024 * 1024,
                    storageAvailable = 36L * 1024 * 1024 * 1024,
                    memoryTotal = 8L * 1024 * 1024 * 1024,
                    memoryAvailable = 4L * 1024 * 1024 * 1024,
                    batteryPercent = 78,
                    charging = true,
                    capturedAt = System.currentTimeMillis(),
                )
            ),
            refresh = {},
        )
    }
}

@Preview(name = "Device Screen - Dark", showBackground = true, widthDp = 375)
@Composable
fun DeviceScreenPreviewDark() {
    KanoTheme(themeMode = KanoThemeMode.DARK) {
        DeviceScreen(
            state = DeviceState.Ready(
                DeviceSnapshot(
                    model = "Google Pixel 8 Pro (Emulator)",
                    androidVersion = "Android 15 (API 35)",
                    storageTotal = 128L * 1024 * 1024 * 1024,
                    storageAvailable = 36L * 1024 * 1024 * 1024,
                    memoryTotal = 8L * 1024 * 1024 * 1024,
                    memoryAvailable = 4L * 1024 * 1024 * 1024,
                    batteryPercent = 78,
                    charging = true,
                    capturedAt = System.currentTimeMillis(),
                )
            ),
            refresh = {},
        )
    }
}

// --- Style Studio Previews ---
@Preview(name = "Style Studio - Light", showBackground = true, widthDp = 375)
@Composable
fun StyleScreenPreviewLight() {
    KanoTheme(themeMode = KanoThemeMode.LIGHT) {
        StyleScreen()
    }
}

@Preview(name = "Style Studio - Dark", showBackground = true, widthDp = 375)
@Composable
fun StyleScreenPreviewDark() {
    KanoTheme(themeMode = KanoThemeMode.DARK) {
        StyleScreen()
    }
}

// --- Personal Care Previews ---
@Preview(name = "Personal Care - Light", showBackground = true, widthDp = 375)
@Composable
fun PersonalCareScreenPreviewLight() {
    KanoTheme(themeMode = KanoThemeMode.LIGHT) {
        PersonalCareScreen()
    }
}

@Preview(name = "Personal Care - Dark", showBackground = true, widthDp = 375)
@Composable
fun PersonalCareScreenPreviewDark() {
    KanoTheme(themeMode = KanoThemeMode.DARK) {
        PersonalCareScreen()
    }
}

// --- Settings Previews ---
@Preview(name = "Settings - Light", showBackground = true, widthDp = 375)
@Composable
fun SettingsScreenPreviewLight() {
    KanoTheme(themeMode = KanoThemeMode.LIGHT) {
        SettingsScreen()
    }
}

@Preview(name = "Settings - Dark", showBackground = true, widthDp = 375)
@Composable
fun SettingsScreenPreviewDark() {
    KanoTheme(themeMode = KanoThemeMode.DARK) {
        SettingsScreen()
    }
}

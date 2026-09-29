package app.kano.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.kano.platform.DeviceSnapshot

@Preview(name = "Home Screen - Light", showBackground = true, widthDp = 375)
@Composable
fun HomeScreenPreview() {
    KanoTheme {
        HomeScreen(
            openDevice = {},
            openMedia = {},
            openStyle = {},
            openPersonalCare = {},
        )
    }
}

@Preview(name = "Device Screen - Ready State", showBackground = true, widthDp = 375)
@Composable
fun DeviceScreenPreview() {
    KanoTheme {
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

@Preview(name = "Style Studio Screen", showBackground = true, widthDp = 375)
@Composable
fun StyleScreenPreview() {
    KanoTheme {
        StyleScreen()
    }
}

@Preview(name = "Personal Care Screen", showBackground = true, widthDp = 375)
@Composable
fun PersonalCareScreenPreview() {
    KanoTheme {
        PersonalCareScreen()
    }
}

@Preview(name = "Settings & Privacy Screen", showBackground = true, widthDp = 375)
@Composable
fun SettingsScreenPreview() {
    KanoTheme {
        SettingsScreen()
    }
}

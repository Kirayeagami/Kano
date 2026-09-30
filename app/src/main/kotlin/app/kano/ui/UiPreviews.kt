package app.kano.ui
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.*
class KanoThemesPreview : PreviewParameterProvider<KanoVisualTheme> {
    override val values = KanoVisualTheme.entries.asSequence()
}
@Preview(name = "Native Home • Light", showBackground = true, widthDp = 360)
@Composable
fun HomeLight(@PreviewParameter(KanoThemesPreview::class) theme: KanoVisualTheme) {
    KanoTheme(KanoThemeMode.LIGHT, theme) { HomeScreen({}, {}, {}, {}) }
}
@Preview(name = "Native Home • Dark", showBackground = true, widthDp = 360)
@Composable
fun HomeDark(@PreviewParameter(KanoThemesPreview::class) theme: KanoVisualTheme) {
    KanoTheme(KanoThemeMode.DARK, theme) { HomeScreen({}, {}, {}, {}) }
}
@Preview(name = "Native Device • loading", showBackground = true, widthDp = 360)
@Composable
fun DevicePreview() { KanoTheme { DeviceScreen(DeviceState.Loading, {}) } }
@Preview(name = "Native Care • 200% text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
fun CarePreview() { KanoTheme { PersonalCareScreen() } }
@Preview(name = "Native Style", showBackground = true, widthDp = 360)
@Composable
fun StylePreview() { KanoTheme { StyleScreen() } }
@Preview(name = "Native Settings", showBackground = true, widthDp = 360)
@Composable
fun SettingsPreview() { KanoTheme { SettingsScreen() } }

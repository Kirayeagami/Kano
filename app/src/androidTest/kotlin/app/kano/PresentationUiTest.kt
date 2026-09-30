package app.kano
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import app.kano.ui.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Actual native screenshots for visual inspection. Run on the emulator, never personal phone data. */
class PresentationUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun capture(name: String) {
        compose.waitForIdle()
        // Wait for the platform renderer too: theme StateFlow delivery and draw are asynchronous.
        android.os.SystemClock.sleep(300)
        compose.waitForIdle()
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        val dir = instrumentation.targetContext.getExternalFilesDir("validation")
            ?: instrumentation.targetContext.filesDir
        val file = File(dir, "$name.png")
        file.parentFile?.mkdirs()
        try {
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        } catch (_: Exception) { }
    }
    private fun clickNav(name: String) { compose.onNodeWithContentDescription(name).performClick() }
    private fun menu(name: String) {
        clickNav("Open menu")
        compose.onNodeWithText(name).performScrollTo().performClick()
    }
    private fun scrollClick(text: String) {
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)).performScrollToNode(hasText(text))
        compose.onNodeWithText(text).performClick()
    }
    @Test fun allNativeRoutesAndEightThemesRemainNavigable() {
        val manager = (compose.activity.application as KanoApplication).graph.themeManager
        compose.runOnIdle { manager.setThemeMode(KanoThemeMode.LIGHT); manager.setVisualTheme(KanoVisualTheme.KANO_GLASS); manager.setGlass(true) }
        capture("home")
        clickNav("Device")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Storage").fetchSemanticsNodes().isNotEmpty() }
        capture("device")
        listOf("Storage", "RAM", "Battery", "Connectivity", "Diagnostics", "System Identity", "Security").forEach { panel ->
            if (panel in listOf("Storage", "RAM", "Battery", "Connectivity", "Diagnostics")) {
                compose.onNode(hasText(panel) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performScrollTo().performClick()
            } else scrollClick(panel)
            capture("device-" + panel.lowercase())
            if (panel == "RAM") {
                scrollClick("Optimize RAM")
                compose.waitUntil(10_000) { compose.onAllNodesWithText("RAM after").fetchSemanticsNodes().isNotEmpty() }
                capture("ram-after")
            }
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)).performScrollToIndex(0)
            compose.onNodeWithText("Overview").performScrollTo().performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithText("Device Intelligence").fetchSemanticsNodes().isNotEmpty() }
        }
        clickNav("Media"); capture("gallery")
        clickNav("Vault"); capture("vault")
        listOf("Style", "Care").forEach { screen -> clickNav(screen); capture(screen.lowercase()) }
        listOf("Shopping", "Daily Life").forEach { screen ->
            menu(screen); capture(screen.lowercase().replace(' ', '-'))
        }
        menu("Settings"); compose.onNodeWithText("Appearance").performClick()
        for (theme in KanoVisualTheme.entries) {
            compose.runOnIdle { manager.setVisualTheme(theme); manager.setThemeMode(KanoThemeMode.LIGHT) }
            capture("theme-light-" + theme.name.lowercase())
            compose.runOnIdle { manager.setThemeMode(KanoThemeMode.DARK) }
            capture("theme-dark-" + theme.name.lowercase())
        }
        compose.runOnIdle { manager.setVisualTheme(KanoVisualTheme.KANO_GLASS); manager.setThemeMode(KanoThemeMode.LIGHT); manager.setGlass(true) }
        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Glass OFF"))
        compose.onNodeWithContentDescription("Glass OFF").performClick().assertIsOn()
        compose.onNodeWithText("Glass Auto").assertDoesNotExist()
        compose.onNodeWithText("Glass On").assertDoesNotExist()
        capture("glass-off")
        compose.onNodeWithContentDescription("Glass OFF").performClick().assertIsOff()
        scrollClick("All settings")
        listOf("Privacy", "Permissions", "AI providers", "About").forEach { screen ->
            scrollClick(screen); capture("settings-" + screen.lowercase().replace(' ', '-'))
            scrollClick("All settings")
        }
        compose.runOnIdle { manager.setThemeMode(KanoThemeMode.DARK) }
        listOf("Home", "Device", "Media", "Vault", "Style", "Care").forEach { screen -> clickNav(screen); capture("dark-" + screen.lowercase()) }
        menu("Settings"); capture("dark-settings")
        clickNav("Open menu"); capture("hamburger-dark")
        compose.onNodeWithText("Appearance & themes").performClick()
        compose.runOnIdle { manager.setThemeMode(KanoThemeMode.LIGHT) }
    }
}

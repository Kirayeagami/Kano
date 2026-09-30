package app.kano

import android.graphics.Bitmap
import android.text.format.Formatter
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.ViewModelProvider
import app.kano.ui.KanoViewModel
import app.kano.ui.KanoThemeMode
import app.kano.ui.KanoVisualTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.After
import java.io.File

/** Actual UI and platform data; no mock storage numbers or source-file mutations. */
class StorageUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    // Semantics clicks do not reset OnePlus's inactivity timer. Keep only this
    // test Activity awake; never alter lock-screen authorization or phone settings.
    @Before fun keepTestWindowAwake() {
        compose.activity.runOnUiThread { compose.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    @After fun restoreTestWindow() {
        compose.activity.runOnUiThread { compose.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    private fun chip(text: String) = compose.onNode(hasText(text) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
    private fun storage() {
        compose.onNodeWithContentDescription("Open menu").performClick()
        compose.onNodeWithText("Storage intelligence").performScrollTo().performClick()
        compose.onNodeWithTag("storage-screen").assertExists()
    }
    private fun scrollTo(text: String) = compose.onNodeWithTag("storage-screen").performScrollToNode(hasText(text))
    private fun capture(name: String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(350)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        compose.waitUntil(10_000) { instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() == "app.kano" }
        assertEquals("A screenshot must contain Kano, not the launcher or a permission dialog", "app.kano", instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString())
        val dir = instrumentation.targetContext.getExternalFilesDir("validation/storage") ?: instrumentation.targetContext.filesDir
        dir.mkdirs()
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: error("Platform screenshot unavailable")
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    private fun footer() {
        val density = compose.activity.resources.displayMetrics.density
        val screenWidth = compose.activity.window.decorView.width.toFloat()
        val screenHeight = compose.activity.window.decorView.height.toFloat()
        val bounds = listOf("Home", "Device", "Media", "Vault", "Style", "Care").map { name ->
            compose.onNodeWithContentDescription(name).assertHasClickAction().assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        }
        bounds.forEach {
            assertTrue("Footer touch width must be at least 48dp", it.width >= 47.5f * density)
            assertTrue("Footer touch height must be at least 48dp", it.height >= 47.5f * density)
            assertTrue("Footer action must remain within the window", it.left >= 0 && it.right <= screenWidth && it.top >= 0 && it.bottom <= screenHeight)
        }
        assertTrue("Footer controls share a horizontal baseline", bounds.maxOf { it.center.y } - bounds.minOf { it.center.y } <= density)
    }

    @Test fun phaseOnePanelsRemainReachableBeforeStorageReview() {
        compose.onNodeWithContentDescription("Device").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("System Resources").fetchSemanticsNodes().isNotEmpty() }
        val panels = listOf(
            "Overview" to "System Resources", "Performance" to "SoC / Chipset model", "RAM" to "Available RAM",
            "Storage" to "Volume capacity", "Battery" to "Charge level", "Thermal" to "Thermal status",
            "Connectivity" to "Active network connection", "Hardware" to "Display screen",
            "Sensors" to "Total hardware sensors", "Diagnostics" to "Subsystem Verification"
        )
        panels.forEach { (panel, evidence) ->
            chip(panel).performScrollTo().performClick().assertIsSelected()
            compose.waitUntil(10_000) { compose.onAllNodesWithText(evidence).fetchSemanticsNodes().isNotEmpty() }
            footer()
            capture("phase1-" + panel.lowercase())
        }
        storage()
        compose.onNodeWithText("Device data volume").assertIsDisplayed()
        footer()
    }

    @Test fun realVolumeStoragePagesAndAccessControlsInBothThemes() {
        val manager = (compose.activity.application as KanoApplication).graph.themeManager
        val oldMode = manager.themeMode.value
        val oldVisual = manager.visualTheme.value
        val oldGlass = manager.glass.value
        try {
            storage()
            for (mode in listOf(KanoThemeMode.LIGHT, KanoThemeMode.DARK)) {
                compose.runOnIdle { manager.setVisualTheme(KanoVisualTheme.KANO_GLASS); manager.setThemeMode(mode); manager.setGlass(true) }
                chip("Overview").performScrollTo().performClick()
                // Capacity comes independently from StatFs, not a number seeded for this test.
                val stat = android.os.StatFs(compose.activity.filesDir.path)
                compose.waitUntil(15_000) {
                    compose.onAllNodesWithText(Formatter.formatFileSize(compose.activity, stat.totalBytes)).fetchSemanticsNodes().isNotEmpty()
                }
                footer()
                capture(mode.name.lowercase() + "-overview")
                scrollTo("Manage media access")
                compose.onNodeWithText("Manage media access").assertHasClickAction()
                scrollTo("Choose files"); compose.onNodeWithText("Choose files").assertHasClickAction()
                scrollTo("Choose a folder"); compose.onNodeWithText("Choose a folder").assertHasClickAction()
                capture(mode.name.lowercase() + "-access")
                // The header may be outside the LazyColumn viewport after reading access controls.
                compose.onNodeWithTag("storage-screen").performScrollToIndex(0)
                for (page in listOf("Breakdown", "Duplicates", "Large files", "Downloads", "Temporary", "Files")) {
                    chip(page).performScrollTo().performClick().assertIsSelected()
                    footer()
                    capture(mode.name.lowercase() + "-" + page.lowercase().replace(' ', '-'))
                    compose.onAllNodes(hasText("Delete", substring = true) and hasClickAction()).assertCountEquals(0)
                }
                compose.runOnIdle { manager.setGlass(false) }
                capture(mode.name.lowercase() + "-glass-off")
                compose.runOnIdle { manager.setGlass(true) }
            }
        } finally {
            compose.runOnIdle { manager.setThemeMode(oldMode); manager.setVisualTheme(oldVisual); manager.setGlass(oldGlass) }
        }
    }

    @Test fun restoredReviewPageReconcilesDefaultViewModelFilter() {
        storage()
        fun model(): KanoViewModel {
            val graph = (compose.activity.application as KanoApplication).graph
            return ViewModelProvider(compose.activity, KanoViewModel.factory(graph))[KanoViewModel::class.java]
        }
        listOf("Large files" to "LARGE", "Downloads" to "DOWNLOADS").forEach { (page, expected) ->
            chip(page).performScrollTo().performClick().assertIsSelected()
            compose.waitUntil(5_000) { model().storageFilter.value == expected }
            // Model the fresh ViewModel defaults that accompany process-state restoration.
            // This is an activity saved-state test, not a claim of killing the test process.
            compose.runOnIdle { model().storageFilter.value = "ALL"; model().storagePage.value = 0 }
            compose.activityRule.scenario.recreate()
            chip(page).assertIsSelected()
            compose.waitUntil(5_000) { model().storageFilter.value == expected }
            assertEquals(expected, model().storageFilter.value)
            footer()
        }
    }
}

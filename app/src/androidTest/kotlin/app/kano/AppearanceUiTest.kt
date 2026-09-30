package app.kano

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AppearanceUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun manualThemeControlsSystemBarContrastAndSurvivesRecreation() {
        compose.onNodeWithContentDescription("Open menu").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Appearance").performClick()
        val darkChoice = hasText("Dark") or hasText("Selected · Dark")
        compose.onNode(hasScrollAction()).performScrollToNode(darkChoice)
        compose.onNode(darkChoice).performClick()
        compose.onNodeWithText("Selected · Dark").assertExists()
        compose.runOnIdle {
            val window = compose.activity.window
            assertFalse(WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars)
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Selected · Dark").assertExists()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Light"))
        compose.onNodeWithText("Light").performClick()
        compose.runOnIdle {
            val window = compose.activity.window
            assertTrue(WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars)
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("System"))
        compose.onNodeWithText("System").performClick()
    }
}

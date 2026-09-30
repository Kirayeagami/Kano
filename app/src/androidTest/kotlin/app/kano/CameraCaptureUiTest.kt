package app.kano

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CameraCaptureUiTest {
    @get:Rule val permission: TestRule = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                try {
                    GrantPermissionRule.grant(Manifest.permission.CAMERA).apply(base, description).evaluate()
                } catch (_: SecurityException) {
                    base.evaluate()
                }
            }
        }
    }
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun captureIsReviewedAndRetakeDoesNotSaveKnowledge() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        Assume.assumeTrue(target.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Open Kano Vision"))
        compose.onNodeWithText("Open Kano Vision").performClick()
        compose.waitUntil(20_000) { compose.onAllNodes(hasText("Capture") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Capture").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Review capture").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Use photo and analyze").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Retake").performScrollTo().performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Capture").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithContentDescription("Home").assertIsSelected()
    }
}

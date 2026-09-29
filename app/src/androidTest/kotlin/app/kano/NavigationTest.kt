package app.kano

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeLeadsToRealDeviceReadingsAndScopedMedia() {
        compose.onNodeWithText("View device readings").performClick()
        compose.waitUntil(timeoutMillis = 10_000) { compose.onAllNodes(androidx.compose.ui.test.hasText("Available storage")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Refresh readings"))
        compose.onNodeWithText("Refresh readings").assertIsDisplayed()
        compose.onNodeWithText("Media", useUnmergedTree = true).performClick()
        compose.onNode(hasText("Media") and hasClickAction()).assertIsSelected()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Choose photos or videos"))
        compose.onNodeWithText("Choose photos or videos").assertIsDisplayed()
    }
}

package app.kano

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeLeadsToRealDeviceReadingsAndScopedMedia() {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("View device readings"))
        compose.onNodeWithText("View device readings").performClick()
        compose.waitUntil(timeoutMillis = 10_000) { compose.onAllNodes(hasText("Device Intelligence")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)).performScrollToNode(hasText("Refresh Device Readings"))
        compose.onNodeWithText("Refresh Device Readings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Media").performClick()
        compose.onNodeWithContentDescription("Media").assertIsSelected()
        compose.onNodeWithText("Kano Gallery").assertIsDisplayed()
        compose.onNodeWithContentDescription("Gallery tools").assertIsDisplayed()
    }
}

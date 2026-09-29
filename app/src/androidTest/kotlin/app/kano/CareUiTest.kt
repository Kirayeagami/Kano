package app.kano

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class CareUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun userCanAddEditCancelDeletionAndDeleteTheirOwnProduct() {
        compose.onNodeWithText("Care").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Add product"))
        compose.onNodeWithText("Add product").performClick()
        compose.onNodeWithText("Save product").assertIsNotEnabled()
        compose.onNodeWithText("Product name").performTextInput("TEST ONLY cleanser")
        compose.onNodeWithText("Save product").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Save product").fetchSemanticsNodes().isEmpty() }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("TEST ONLY cleanser"))
        compose.onNodeWithText("Edit").performScrollTo().performClick()
        compose.onNodeWithText("Product name").performTextReplacement("TEST ONLY updated")
        compose.onNodeWithText("Low", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Save product").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Save product").fetchSemanticsNodes().isEmpty() }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Status: Low"))
        compose.activityRule.scenario.recreate()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("TEST ONLY updated"))
        compose.onNodeWithText("Status: Low").assertExists()
        compose.onNodeWithText("Delete", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Keep product").performClick()
        compose.onNodeWithText("TEST ONLY updated").assertExists()
        compose.onNodeWithText("Delete", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Delete product").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("No products saved"))
        compose.onNodeWithText("No products saved").assertIsDisplayed()
    }

    @Test fun styleDoesNotSimulateAnalysis() {
        compose.onNodeWithText("Style").performClick()
        compose.onNodeWithText("Not available yet").assertExists()
        compose.onNodeWithText("Take photo").assertDoesNotExist()
        compose.onNodeWithText("RECOMMENDED").assertDoesNotExist()
    }
}

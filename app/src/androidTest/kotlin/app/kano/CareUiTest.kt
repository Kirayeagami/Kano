package app.kano

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class CareUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun userCanAddEditCancelDeletionAndDeleteTheirOwnProduct() {
        compose.onNodeWithContentDescription("Care").performClick()
        compose.onNodeWithTag("care-list").performScrollToNode(hasText("Add Product to Inventory"))
        compose.onNodeWithText("Add Product to Inventory").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Save Product").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Save Product").assertIsNotEnabled()
        compose.onNodeWithText("Product Name").performTextInput("TEST ONLY cleanser")
        compose.onNodeWithText("Save Product").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Save Product").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("care-list").performScrollToNode(hasText("TEST ONLY cleanser"))
        compose.onNodeWithText("Edit").performScrollTo().performClick()
        compose.onNodeWithText("Product Name").performTextReplacement("TEST ONLY updated")
        compose.onNodeWithText("Low", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Save Product").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Save Product").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("care-list").performScrollToNode(hasText("Low"))
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("care-list").performScrollToNode(hasText("TEST ONLY updated"))
        compose.onNodeWithText("Low").assertExists()
        compose.onNodeWithText("Delete", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Keep Product").performClick()
        compose.onNodeWithText("TEST ONLY updated").assertExists()
        compose.onNodeWithText("Delete", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Delete Product").performClick()
        compose.onNodeWithTag("care-list").performScrollToNode(hasText("No products saved yet."))
        compose.onNodeWithText("No products saved yet.").assertIsDisplayed()
    }

    @Test fun styleDoesNotSimulateAnalysis() {
        compose.onNodeWithContentDescription("Style").performClick()
        compose.onNodeWithText("Dress Me · Not configured").assertExists()
        compose.onNodeWithText("Take photo").assertDoesNotExist()
        compose.onNodeWithText("RECOMMENDED").assertDoesNotExist()
    }
}


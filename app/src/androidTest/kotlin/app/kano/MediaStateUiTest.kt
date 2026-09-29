package app.kano

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.kano.data.MediaRecord
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class MediaStateUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun failedItemIsNotQueuedAndForgetRemainsReachableAfterEmptySearch() {
        val database = (compose.activity.application as KanoApplication).graph.database
        val uri = "content://kano-test/error"
        try {
            runBlocking { database.media().add(MediaRecord(uri, "TEST ONLY failure.png", state = "ERROR")) }
            compose.onNodeWithText("Media", useUnmergedTree = true).performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("1 Selected Documents").fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("ERROR · RETRY INDEX"))
            compose.onNodeWithText("ERROR · RETRY INDEX").assertIsDisplayed()
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Search selected filenames"))
            compose.onNodeWithText("Search selected filenames").performTextInput("no-match-xyz")
            compose.waitUntil(10_000) { compose.onAllNodesWithText("No items match your search on this page.").fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Forget selected media index"))
            compose.onNodeWithText("Forget selected media index").performClick()
            compose.onNodeWithText("Keep index").performClick()
        } finally {
            runBlocking { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                database.openHelper.writableDatabase.execSQL("DELETE FROM media WHERE uri = ?", arrayOf(uri))
            } }
        }
    }
}

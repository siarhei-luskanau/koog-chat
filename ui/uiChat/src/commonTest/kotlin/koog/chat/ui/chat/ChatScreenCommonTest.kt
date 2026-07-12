package koog.chat.ui.chat

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
internal class ChatScreenCommonTest {
    @Test
    fun simpleMode() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Compose Multiplatform").assertIsDisplayed()
        }

    @Test
    fun advancedMode() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Compose Multiplatform").assertIsDisplayed()
            onNodeWithText("Σ 1.2k tokens").assertIsDisplayed()
        }

    @Test
    fun modelPickerLight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewLight() }
            waitForIdle()
            awaitIdle()
            awaitText("Select model")
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).printToLog("StartTag")
            onNodeWithText("Select model").assertIsDisplayed()
        }

    @Test
    fun modelPickerNight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewNight() }
            waitForIdle()
            awaitIdle()
            awaitText("Select model")
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).printToLog("StartTag")
            onNodeWithText("Select model").assertIsDisplayed()
        }

    private suspend fun ComposeUiTest.awaitText(text: String) {
        repeat(100) {
            waitForIdle()
            awaitIdle()
            if (onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()) return
            withContext(Dispatchers.Default) { delay(50.milliseconds) }
        }
    }
}

package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatScreenCommonTest {
    @Test
    fun simpleMode() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Compose Multiplatform").assertIsDisplayed()
        }

    @Test
    fun advancedMode() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Compose Multiplatform").assertIsDisplayed()
            onNodeWithText("Σ 1.2k tokens").assertIsDisplayed()
        }
}

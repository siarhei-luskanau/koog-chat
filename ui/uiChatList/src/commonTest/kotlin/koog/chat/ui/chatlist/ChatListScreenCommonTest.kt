package koog.chat.ui.chatlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatListScreenCommonTest {
    @Test
    fun simpleCheck() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewLight() }
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("TODAY").assertIsDisplayed()
        }
}

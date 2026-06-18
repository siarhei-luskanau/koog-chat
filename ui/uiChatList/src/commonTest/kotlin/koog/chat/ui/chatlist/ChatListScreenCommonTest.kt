package koog.chat.ui.chatlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatListScreenCommonTest {
    @Test
    fun success() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
        }

    @Test
    fun empty() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
        }
}

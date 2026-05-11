package koog.chat.ui.chatlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
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
            // onAllNodes(hasClickAction()).assertCountEquals(3)
        }
}

package koog.chat.ui.chatlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class ChatListScreenIosTest {
    @Test
    fun successPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(
                this,
                filePath = "koog.chat.ui.chatlist.ChatListScreenIosTest.successPreviewLight.png",
            )
        }

    @Test
    fun emptyPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(
                this,
                filePath = "koog.chat.ui.chatlist.ChatListScreenIosTest.emptyPreviewLight.png",
            )
        }

    @Test
    fun loadingPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(
                this,
                filePath = "koog.chat.ui.chatlist.ChatListScreenIosTest.loadingPreviewLight.png",
            )
        }
}

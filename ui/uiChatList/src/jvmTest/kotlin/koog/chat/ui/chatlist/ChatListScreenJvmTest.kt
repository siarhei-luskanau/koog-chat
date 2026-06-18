package koog.chat.ui.chatlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatListScreenJvmTest {
    @Test
    fun successPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun emptyPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun loadingPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenLoadingPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

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
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun emptyPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun loadingPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

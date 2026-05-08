package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatListItemJvmTest {
    @Test
    fun chatListItemLight() =
        runDesktopComposeUiTest {
            setContent { ChatListItemPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

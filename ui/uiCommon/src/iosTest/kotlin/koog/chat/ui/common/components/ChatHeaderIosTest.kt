package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class ChatHeaderIosTest {
    @Test
    fun chatHeaderSimpleLight() =
        runComposeUiTest {
            setContent { ChatHeaderSimplePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.ChatHeaderIosTest.chatHeaderSimpleLight.png")
        }

    @Test
    fun chatHeaderAdvancedLight() =
        runComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.ChatHeaderIosTest.chatHeaderAdvancedLight.png")
        }
}

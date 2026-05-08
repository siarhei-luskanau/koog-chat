package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class ChatHeaderIosTest {
    @Test
    fun chatHeaderSimpleLight() =
        runSkikoComposeUiTest {
            setContent { ChatHeaderSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.ChatHeaderIosTest.chatHeaderSimpleLight.png")
        }

    @Test
    fun chatHeaderAdvancedLight() =
        runSkikoComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.ChatHeaderIosTest.chatHeaderAdvancedLight.png")
        }
}

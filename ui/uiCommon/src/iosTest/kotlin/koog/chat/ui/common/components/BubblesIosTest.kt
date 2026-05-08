package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class BubblesIosTest {
    @Test
    fun userBubbleLight() =
        runSkikoComposeUiTest {
            setContent { UserBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.userBubbleLight.png")
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runSkikoComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.assistantBubbleSimpleLight.png")
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runSkikoComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.assistantBubbleAdvancedLight.png")
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runSkikoComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.thinkingBlockStreamingLight.png")
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runSkikoComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.thinkingBlockAdvancedLight.png")
        }

    @Test
    fun errorBubbleLight() =
        runSkikoComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.errorBubbleLight.png")
        }
}

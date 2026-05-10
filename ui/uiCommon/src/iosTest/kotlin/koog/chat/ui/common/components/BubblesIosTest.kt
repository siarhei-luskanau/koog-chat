package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class BubblesIosTest {
    @Test
    fun userBubbleLight() =
        runComposeUiTest {
            setContent { UserBubblePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.userBubbleLight.png")
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.assistantBubbleSimpleLight.png")
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.assistantBubbleAdvancedLight.png")
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.thinkingBlockStreamingLight.png")
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.thinkingBlockAdvancedLight.png")
        }

    @Test
    fun errorBubbleLight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.BubblesIosTest.errorBubbleLight.png")
        }
}

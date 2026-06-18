package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class BubblesJvmTest {
    @Test
    fun userBubbleLight() =
        runComposeUiTest {
            setContent { UserBubblePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorBubbleLight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class BubblesJvmTest {
    @Test
    fun userBubbleLight() =
        runDesktopComposeUiTest {
            setContent { UserBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runDesktopComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runDesktopComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runDesktopComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runDesktopComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorBubbleLight() =
        runDesktopComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

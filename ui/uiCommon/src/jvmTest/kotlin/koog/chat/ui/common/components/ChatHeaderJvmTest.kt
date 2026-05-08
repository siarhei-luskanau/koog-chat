package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatHeaderJvmTest {
    @Test
    fun chatHeaderSimpleLight() =
        runDesktopComposeUiTest {
            setContent { ChatHeaderSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun chatHeaderAdvancedLight() =
        runDesktopComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

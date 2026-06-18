package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatHeaderJvmTest {
    @Test
    fun chatHeaderSimpleLight() =
        runComposeUiTest {
            setContent { ChatHeaderSimplePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun chatHeaderAdvancedLight() =
        runComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

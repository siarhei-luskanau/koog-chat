package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatScreenJvmTest {
    @Test
    fun previewSimpleLight() =
        runDesktopComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewAdvancedLight() =
        runDesktopComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewLoadingLight() =
        runDesktopComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class InputBarJvmTest {
    @Test
    fun inputBarEmptyLight() =
        runDesktopComposeUiTest {
            setContent { InputBarEmptyPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun inputBarFilledLight() =
        runDesktopComposeUiTest {
            setContent { InputBarFilledPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

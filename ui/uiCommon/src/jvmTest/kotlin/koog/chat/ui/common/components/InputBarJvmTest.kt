package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class InputBarJvmTest {
    @Test
    fun inputBarEmptyLight() =
        runComposeUiTest {
            setContent { InputBarEmptyPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun inputBarFilledLight() =
        runComposeUiTest {
            setContent { InputBarFilledPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

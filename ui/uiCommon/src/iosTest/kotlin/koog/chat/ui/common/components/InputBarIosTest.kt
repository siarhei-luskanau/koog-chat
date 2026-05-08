package koog.chat.ui.common.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class InputBarIosTest {
    @Test
    fun inputBarEmptyLight() =
        runSkikoComposeUiTest {
            setContent { InputBarEmptyPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.InputBarIosTest.inputBarEmptyLight.png")
        }

    @Test
    fun inputBarFilledLight() =
        runSkikoComposeUiTest {
            setContent { InputBarFilledPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.components.InputBarIosTest.inputBarFilledLight.png")
        }
}

package koog.chat.ui.main

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

internal class MainScreenJvmTest {
    @Test
    fun preview() =
        runComposeUiTest {
            setContent { MainScreenPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

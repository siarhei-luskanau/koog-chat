package koog.chat.ui.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class MainScreenJvmTest {
    @Test
    fun previewLight() =
        runDesktopComposeUiTest {
            setContent { MainScreenPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

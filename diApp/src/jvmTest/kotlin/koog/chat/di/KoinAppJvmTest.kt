package koog.chat.di

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class KoinAppJvmTest {
    @Test
    fun previewLight() =
        runDesktopComposeUiTest {
            setContent { KoinAppPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

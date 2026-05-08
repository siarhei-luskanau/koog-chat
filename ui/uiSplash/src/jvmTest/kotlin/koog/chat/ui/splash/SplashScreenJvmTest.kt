package koog.chat.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SplashScreenJvmTest {
    @Test
    fun loadingLight() =
        runDesktopComposeUiTest {
            setContent { SplashScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun successLight() =
        runDesktopComposeUiTest {
            setContent { SplashScreenSuccessPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorLight() =
        runDesktopComposeUiTest {
            setContent { SplashScreenErrorPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

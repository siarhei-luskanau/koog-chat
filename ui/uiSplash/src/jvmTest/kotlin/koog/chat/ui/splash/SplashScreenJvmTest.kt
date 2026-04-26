package koog.chat.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SplashScreenJvmTest {
    @Test
    fun loading() =
        runDesktopComposeUiTest {
            setContent { SplashScreenLoadingPreview() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun success() =
        runDesktopComposeUiTest {
            setContent { SplashScreenSuccessPreview() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun error() =
        runDesktopComposeUiTest {
            setContent { SplashScreenErrorPreview() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

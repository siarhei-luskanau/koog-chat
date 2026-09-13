package template.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SplashScreenJvmTest {
    @Test
    fun loading() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun success() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun error() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

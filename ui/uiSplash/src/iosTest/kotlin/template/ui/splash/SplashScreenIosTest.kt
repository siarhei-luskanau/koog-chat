package template.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class SplashScreenIosTest {
    @Test
    fun loading() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "template.ui.splash.SplashScreenIosTest.loading.png")
        }

    @Test
    fun success() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "template.ui.splash.SplashScreenIosTest.success.png")
        }

    @Test
    fun error() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "template.ui.splash.SplashScreenIosTest.error.png")
        }
}

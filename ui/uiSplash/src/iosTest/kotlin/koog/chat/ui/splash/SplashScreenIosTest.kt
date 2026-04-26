package koog.chat.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class SplashScreenIosTest {
    @Test
    fun loading() =
        runSkikoComposeUiTest {
            setContent { SplashScreenLoadingPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.loading.png")
        }

    @Test
    fun success() =
        runSkikoComposeUiTest {
            setContent { SplashScreenSuccessPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.success.png")
        }

    @Test
    fun error() =
        runSkikoComposeUiTest {
            setContent { SplashScreenErrorPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.error.png")
        }
}

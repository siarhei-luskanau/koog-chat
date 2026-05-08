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
    fun loadingLight() =
        runSkikoComposeUiTest {
            setContent { SplashScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.loadingLight.png")
        }

    @Test
    fun successLight() =
        runSkikoComposeUiTest {
            setContent { SplashScreenSuccessPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.successLight.png")
        }

    @Test
    fun errorLight() =
        runSkikoComposeUiTest {
            setContent { SplashScreenErrorPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.errorLight.png")
        }
}

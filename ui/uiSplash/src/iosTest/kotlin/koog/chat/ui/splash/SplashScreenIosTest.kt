package koog.chat.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class SplashScreenIosTest {
    @Test
    fun loadingLight() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.loadingLight.png")
        }

    @Test
    fun successLight() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.successLight.png")
        }

    @Test
    fun errorLight() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.splash.SplashScreenIosTest.errorLight.png")
        }
}

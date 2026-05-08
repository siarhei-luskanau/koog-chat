package koog.chat.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.SmallPhone)
@OptIn(ExperimentalTestApi::class)
internal class SplashScreenAndroidTest {
    @Test
    fun loadingLight() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun loadingNight() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun successLight() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun successNight() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorLight() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun errorNight() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

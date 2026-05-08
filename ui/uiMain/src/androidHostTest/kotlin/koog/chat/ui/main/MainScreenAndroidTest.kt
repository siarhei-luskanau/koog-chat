package koog.chat.ui.main

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
internal class MainScreenAndroidTest {
    @Test
    fun previewLight() =
        runComposeUiTest {
            setContent { MainScreenPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun previewNight() =
        runComposeUiTest {
            setContent { MainScreenPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

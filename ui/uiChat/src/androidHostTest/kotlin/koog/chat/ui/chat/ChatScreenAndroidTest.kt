package koog.chat.ui.chat

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
internal class ChatScreenAndroidTest {
    @Test
    fun previewSimpleLight() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun previewSimpleNight() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewAdvancedLight() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun previewAdvancedNight() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewLoadingLight() =
        runComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewLoadingNight() =
        runComposeUiTest {
            setContent { ChatScreenLoadingPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

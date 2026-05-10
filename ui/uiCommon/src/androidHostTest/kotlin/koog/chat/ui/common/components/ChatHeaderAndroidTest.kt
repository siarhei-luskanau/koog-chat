package koog.chat.ui.common.components

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
internal class ChatHeaderAndroidTest {
    @Test
    fun chatHeaderSimpleLight() =
        runComposeUiTest {
            setContent { ChatHeaderSimplePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun chatHeaderSimpleNight() =
        runComposeUiTest {
            setContent { ChatHeaderSimplePreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun chatHeaderAdvancedLight() =
        runComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun chatHeaderAdvancedNight() =
        runComposeUiTest {
            setContent { ChatHeaderAdvancedPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

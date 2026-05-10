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
internal class InputBarAndroidTest {
    @Test
    fun inputBarEmptyLight() =
        runComposeUiTest {
            setContent { InputBarEmptyPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun inputBarEmptyNight() =
        runComposeUiTest {
            setContent { InputBarEmptyPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun inputBarFilledLight() =
        runComposeUiTest {
            setContent { InputBarFilledPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun inputBarFilledNight() =
        runComposeUiTest {
            setContent { InputBarFilledPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

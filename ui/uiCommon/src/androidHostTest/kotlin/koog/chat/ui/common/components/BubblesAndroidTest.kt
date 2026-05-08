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
internal class BubblesAndroidTest {
    @Test
    fun userBubbleLight() =
        runComposeUiTest {
            setContent { UserBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun userBubbleNight() =
        runComposeUiTest {
            setContent { UserBubblePreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun assistantBubbleSimpleNight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun assistantBubbleAdvancedNight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun thinkingBlockStreamingNight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun thinkingBlockAdvancedNight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorBubbleLight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun errorBubbleNight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewNight() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

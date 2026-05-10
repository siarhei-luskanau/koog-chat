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
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun userBubbleNight() =
        runComposeUiTest {
            setContent { UserBubblePreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleSimpleLight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun assistantBubbleSimpleNight() =
        runComposeUiTest {
            setContent { AssistantBubbleSimplePreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun assistantBubbleAdvancedLight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun assistantBubbleAdvancedNight() =
        runComposeUiTest {
            setContent { AssistantBubbleAdvancedPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockStreamingLight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun thinkingBlockStreamingNight() =
        runComposeUiTest {
            setContent { ThinkingBlockStreamingPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun thinkingBlockAdvancedLight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun thinkingBlockAdvancedNight() =
        runComposeUiTest {
            setContent { ThinkingBlockAdvancedPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun errorBubbleLight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun errorBubbleNight() =
        runComposeUiTest {
            setContent { ErrorBubblePreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

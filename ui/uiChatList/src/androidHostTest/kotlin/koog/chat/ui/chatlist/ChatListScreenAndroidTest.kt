package koog.chat.ui.chatlist

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
internal class ChatListScreenAndroidTest {
    @Test
    fun successPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun successPreviewNight() =
        runComposeUiTest {
            setContent { ChatListScreenSuccessPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun emptyPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun emptyPreviewNight() =
        runComposeUiTest {
            setContent { ChatListScreenEmptyPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun loadingPreviewLight() =
        runComposeUiTest {
            setContent { ChatListScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    @Config(qualifiers = "+night")
    fun loadingPreviewNight() =
        runComposeUiTest {
            setContent { ChatListScreenLoadingPreviewNight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

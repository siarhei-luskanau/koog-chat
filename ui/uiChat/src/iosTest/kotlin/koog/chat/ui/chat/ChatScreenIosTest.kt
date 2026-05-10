package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class ChatScreenIosTest {
    @Test
    fun previewSimpleLight() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewSimpleLight.png")
        }

    @Test
    fun previewAdvancedLight() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewAdvancedLight.png")
        }

    @Test
    fun previewLoadingLight() =
        runComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewLoadingLight.png")
        }
}

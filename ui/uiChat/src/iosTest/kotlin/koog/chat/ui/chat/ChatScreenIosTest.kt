package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class ChatScreenIosTest {
    @Test
    fun previewSimpleLight() =
        runSkikoComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewSimpleLight.png")
        }

    @Test
    fun previewAdvancedLight() =
        runSkikoComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewAdvancedLight.png")
        }

    @Test
    fun previewLoadingLight() =
        runSkikoComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewLoadingLight.png")
        }
}

package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
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

    @Test
    fun previewModelPickerLight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewLight() }
            awaitIdle()
            onNode(isRoot() and hasAnyDescendant(hasText("Select model")))
                .captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewModelPickerLight.png")
        }

    @Test
    fun previewModelPickerNight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewNight() }
            awaitIdle()
            onNode(isRoot() and hasAnyDescendant(hasText("Select model")))
                .captureRoboImage(this, filePath = "koog.chat.ui.chat.ChatScreenIosTest.previewModelPickerNight.png")
        }
}

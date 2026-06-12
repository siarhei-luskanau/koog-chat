package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatScreenJvmTest {
    @Test
    fun previewSimpleLight() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewAdvancedLight() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewLoadingLight() =
        runComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewModelPickerLight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewLight() }
            awaitIdle()
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).captureRoboImage()
        }

    @Test
    fun previewModelPickerNight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewNight() }
            awaitIdle()
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).captureRoboImage()
        }
}

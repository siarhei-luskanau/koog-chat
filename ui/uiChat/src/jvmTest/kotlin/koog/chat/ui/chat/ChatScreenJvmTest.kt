package koog.chat.ui.chat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ChatScreenJvmTest {
    @Test
    fun previewSimpleLight() =
        runComposeUiTest {
            setContent { ChatScreenSimplePreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewAdvancedLight() =
        runComposeUiTest {
            setContent { ChatScreenAdvancedPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewLoadingLight() =
        runComposeUiTest {
            setContent { ChatScreenLoadingPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun previewModelPickerLight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewLight() }
            waitForIdle()
            awaitIdle()
            waitUntilAtLeastOneExists(hasText("Select model"), timeoutMillis = 5_000L)
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).captureRoboImage()
        }

    @Test
    fun previewModelPickerNight() =
        runComposeUiTest {
            setContent { ChatScreenModelPickerPreviewNight() }
            waitForIdle()
            awaitIdle()
            waitUntilAtLeastOneExists(hasText("Select model"), timeoutMillis = 5_000L)
            onNode(isRoot() and hasAnyDescendant(hasText("Select model"))).captureRoboImage()
        }
}

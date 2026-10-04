package koog.chat.di

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

internal class KoinAppJvmTest {
    @Test
    fun preview() =
        runComposeUiTest {
            setContent { KoinAppPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

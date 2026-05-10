package koog.chat.di

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class KoinAppJvmTest {
    @Test
    fun previewLight() =
        runComposeUiTest {
            setContent { KoinAppPreviewLight() }
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

package koog.chat.ui.common.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class AppThemeJvmTest {
    @Test
    fun colors() =
        runDesktopComposeUiTest {
            setContent { AppThemeColorsPreview() }
            waitForIdle()
            onRoot().captureRoboImage()
        }
}

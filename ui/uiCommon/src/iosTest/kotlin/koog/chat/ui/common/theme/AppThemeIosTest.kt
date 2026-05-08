package koog.chat.ui.common.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class AppThemeIosTest {
    @Test
    fun colors() =
        runSkikoComposeUiTest {
            setContent { AppThemeColorsPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "koog.chat.ui.common.theme.AppThemeIosTest.colors.png")
        }
}

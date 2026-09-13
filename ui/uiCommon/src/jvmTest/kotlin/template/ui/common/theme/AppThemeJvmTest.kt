package template.ui.common.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class AppThemeJvmTest {
    @Test
    fun colors() =
        runComposeUiTest {
            setContent { AppThemeColorsPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage()
        }
}

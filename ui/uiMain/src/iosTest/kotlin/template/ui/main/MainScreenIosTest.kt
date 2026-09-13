package template.ui.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class MainScreenIosTest {
    @Test
    fun preview() =
        runComposeUiTest {
            setContent { MainScreenPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().captureRoboImage(this, filePath = "template.ui.main.MainScreenIosTest.preview.png")
        }
}

package template.ui.splash

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SplashScreenCommonTest {
    @Test
    fun loading() =
        runComposeUiTest {
            setContent { SplashScreenLoadingPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Loading").assertIsDisplayed()
        }

    @Test
    fun success() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Preview").assertIsDisplayed()
        }

    @Test
    fun error() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreview() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Error").assertIsDisplayed()
        }
}

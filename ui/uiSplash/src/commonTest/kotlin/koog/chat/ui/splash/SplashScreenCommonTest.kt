package koog.chat.ui.splash

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
            setContent { SplashScreenLoadingPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Loading").assertIsDisplayed()
        }

    @Test
    fun success() =
        runComposeUiTest {
            setContent { SplashScreenSuccessPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Preview").assertIsDisplayed()
        }

    @Test
    fun error() =
        runComposeUiTest {
            setContent { SplashScreenErrorPreviewLight() }
            waitForIdle()
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Splash: Error").assertIsDisplayed()
        }
}

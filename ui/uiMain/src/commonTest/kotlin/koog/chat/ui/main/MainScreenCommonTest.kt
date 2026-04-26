package koog.chat.ui.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class MainScreenCommonTest {
    @Test
    fun simpleCheck() =
        runComposeUiTest {
            setContent { MainScreenPreview() }
            waitForIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Main: Preview").assertIsDisplayed()
        }
}

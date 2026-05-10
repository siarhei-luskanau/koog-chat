package koog.chat.di

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class KoinAppCommonTest {
    @Test
    fun simpleCheck() =
        runComposeUiTest {
            setContent { KoinApp() }
            awaitIdle()
            onRoot().printToLog("StartTag")
            onNodeWithContentDescription("KoinApp").assertIsDisplayed()
        }
}

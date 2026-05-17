package koog.chat.di

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.waitUntilDoesNotExist
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class NewChatFlowCommonTest {
    @Test
    fun test() =
        runComposeUiTest {
            setContent { KoinApp() }
            awaitIdle()
            try {
                waitUntilAtLeastOneExists(hasText("No chats yet"), timeoutMillis = 5_000L)
            } catch (_: ComposeTimeoutException) {
                return@runComposeUiTest
            }
            onNodeWithText("No chats yet").assertIsDisplayed()
            onNodeWithText("New chat", useUnmergedTree = true).performClick()
            awaitIdle()
            waitUntilAtLeastOneExists(hasContentDescription("Back"), timeoutMillis = 5_000L)
            onNodeWithContentDescription("Back").performClick()
            awaitIdle()
            waitUntilDoesNotExist(hasText("No chats yet"), timeoutMillis = 5_000L)
            waitUntilAtLeastOneExists(hasText("Chat with a user"), timeoutMillis = 5_000L)
            onNodeWithText("Chat with a user").assertIsDisplayed()
        }
}

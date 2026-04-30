package koog.chat.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import koog.chat.ui.chat.ChatNavigationCallback
import koog.chat.ui.chatlist.ChatListNavigationCallback
import koog.chat.ui.llmconfig.LlmConfigNavigationCallback
import koog.chat.ui.splash.SplashNavigationCallback
import org.koin.core.annotation.Single

@Single
internal class AppNavigation :
    ChatListNavigationCallback,
    ChatNavigationCallback,
    LlmConfigNavigationCallback,
    SplashNavigationCallback {
    val backStack = mutableStateListOf<NavKey>(AppRoutes.Splash)

    override fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    override fun goMainScreen() {
        backStack.add(AppRoutes.Main)
        backStack.remove(AppRoutes.Splash)
    }

    override fun openChat(chatId: String) {
        backStack.add(AppRoutes.Chat(chatId = chatId))
    }

    override fun openNewChat() {
        backStack.add(AppRoutes.Chat(chatId = "new"))
    }

    override fun openLlmConfigList() {
        backStack.add(AppRoutes.LlmConfigList)
    }

    override fun openConfigDetails(configId: String?) {
        backStack.add(AppRoutes.LlmConfigDetails(configId = configId))
    }
}

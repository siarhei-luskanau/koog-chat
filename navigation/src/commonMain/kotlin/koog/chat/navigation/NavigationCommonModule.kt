package koog.chat.navigation

import koog.chat.ui.chat.ChatScreen
import koog.chat.ui.chatlist.ChatListScreen
import koog.chat.ui.llmconfig.details.LlmConfigDetailsScreen
import koog.chat.ui.llmconfig.list.LlmConfigListScreen
import koog.chat.ui.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Module
@ComponentScan(value = ["koog.chat.navigation"])
class NavigationCommonModule

@OptIn(KoinExperimentalAPI::class)
val navigationModule =
    module {
        navigation<AppRoutes.Splash> {
            SplashScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.Main> {
            ChatListScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.Chat> { route ->
            ChatScreen(viewModel = koinViewModel(key = route.chatId) { parametersOf(route.chatId) })
        }
        navigation<AppRoutes.LlmConfigList> {
            LlmConfigListScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.LlmConfigDetails> { route ->
            LlmConfigDetailsScreen(viewModel = koinViewModel(key = route.configId ?: "new") { parametersOf(route.configId) })
        }
    }

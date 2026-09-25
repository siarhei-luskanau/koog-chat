package koog.chat.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import koog.chat.ui.main.MainNavigationCallback
import koog.chat.ui.splash.SplashNavigationCallback
import org.koin.core.annotation.Single

@Single
internal class AppNavigation :
    MainNavigationCallback,
    SplashNavigationCallback {
    val backStack = mutableStateListOf<NavKey>(AppRoutes.Splash)

    override fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    override fun goMainScreen(initArg: String) {
        backStack.add(AppRoutes.Main(initArg = initArg))
        backStack.remove(AppRoutes.Splash)
    }
}

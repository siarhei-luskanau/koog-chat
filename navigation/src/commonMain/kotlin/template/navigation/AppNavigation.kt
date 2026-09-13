package template.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import org.koin.core.annotation.Single
import template.ui.main.MainNavigationCallback
import template.ui.splash.SplashNavigationCallback

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

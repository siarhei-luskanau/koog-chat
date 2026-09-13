package template.navigation

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import template.ui.main.MainScreen
import template.ui.splash.SplashScreen

@Module
@ComponentScan(value = ["template.navigation"])
class NavigationCommonModule

@OptIn(KoinExperimentalAPI::class)
val navigationModule =
    module {
        navigation<AppRoutes.Splash> {
            SplashScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.Main> { route ->
            MainScreen(viewModel = koinViewModel { parametersOf(route.initArg) })
        }
    }

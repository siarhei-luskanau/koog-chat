package koog.chat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import koog.chat.ui.common.theme.AppTheme
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.mp.KoinPlatform.getKoin

@OptIn(KoinExperimentalAPI::class)
@Composable
fun NavApp() {
    val koin = getKoin()
    val appNavigation: AppNavigation = koin.get()
    AppTheme {
        NavDisplay(
            backStack = appNavigation.backStack,
            onBack = { appNavigation.goBack() },
            entryProvider = koinEntryProvider(),
        )
    }
}

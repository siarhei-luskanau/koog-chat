package template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.mp.KoinPlatform.getKoin
import template.ui.common.theme.AppTheme

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

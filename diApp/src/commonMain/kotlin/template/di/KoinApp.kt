package template.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.plugin.module.dsl.koinConfiguration
import template.navigation.NavApp
import template.navigation.navigationModule
import template.ui.common.theme.AppTheme

@Composable
fun KoinApp() =
    KoinApplication(
        configuration =
            KoinConfiguration {
                koinConfiguration<DiKoinApplication>().config.invoke(this)
                modules(navigationModule)
            },
    ) {
        NavApp()
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun KoinAppPreview() = AppTheme { KoinApp() }

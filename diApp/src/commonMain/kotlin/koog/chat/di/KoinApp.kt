package koog.chat.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import koog.chat.navigation.NavApp
import koog.chat.navigation.navigationModule
import koog.chat.ui.common.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.plugin.module.dsl.koinConfiguration

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
@Composable
internal fun KoinAppPreviewLight() = AppTheme { KoinApp() }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun KoinAppPreviewNight() = AppTheme { KoinApp() }

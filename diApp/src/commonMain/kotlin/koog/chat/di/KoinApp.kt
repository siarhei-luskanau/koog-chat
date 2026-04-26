package koog.chat.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import koog.chat.navigation.NavApp
import koog.chat.navigation.navigationModule
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.plugin.module.dsl.koinConfiguration

@Preview
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

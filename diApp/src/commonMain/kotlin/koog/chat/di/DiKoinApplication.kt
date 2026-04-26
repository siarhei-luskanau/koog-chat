package koog.chat.di

import koog.chat.core.common.CoreCommonCommonModule
import koog.chat.core.pref.CorePrefCommonModule
import koog.chat.navigation.NavigationCommonModule
import koog.chat.ui.main.MainCommonModule
import koog.chat.ui.splash.SplashCommonModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        CoreCommonCommonModule::class,
        CorePrefCommonModule::class,
        DiCommonModule::class,
        MainCommonModule::class,
        NavigationCommonModule::class,
        SplashCommonModule::class,
    ],
)
internal class DiKoinApplication

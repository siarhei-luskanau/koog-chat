package template.di

import org.koin.core.annotation.KoinApplication
import template.core.common.CoreCommonCommonModule
import template.core.database.room.CoreDatabaseRoomCommonModule
import template.core.pref.CorePrefCommonModule
import template.navigation.NavigationCommonModule
import template.ui.main.MainCommonModule
import template.ui.splash.SplashCommonModule

@KoinApplication(
    modules = [
        CoreCommonCommonModule::class,
        CoreDatabaseRoomCommonModule::class,
        CorePrefCommonModule::class,
        DiCommonModule::class,
        MainCommonModule::class,
        NavigationCommonModule::class,
        SplashCommonModule::class,
    ],
)
internal class DiKoinApplication

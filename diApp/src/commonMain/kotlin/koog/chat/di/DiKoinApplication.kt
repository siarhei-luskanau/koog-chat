package koog.chat.di

import koog.chat.core.common.CoreCommonCommonModule
import koog.chat.core.database.room.CoreDatabaseRoomCommonModule
import koog.chat.core.pref.CorePrefCommonModule
import koog.chat.navigation.NavigationCommonModule
import koog.chat.ui.chat.ChatCommonModule
import koog.chat.ui.chatlist.ChatListCommonModule
import koog.chat.ui.splash.SplashCommonModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        ChatCommonModule::class,
        ChatCommonModule::class,
        ChatListCommonModule::class,
        CoreCommonCommonModule::class,
        CoreDatabaseRoomCommonModule::class,
        CorePrefCommonModule::class,
        DiCommonModule::class,
        NavigationCommonModule::class,
        SplashCommonModule::class,
    ],
)
internal class DiKoinApplication

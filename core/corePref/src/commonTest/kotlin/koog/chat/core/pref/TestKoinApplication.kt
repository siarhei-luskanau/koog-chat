package koog.chat.core.pref

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        CorePrefCommonModule::class,
        CorePrefCommonTestModule::class,
    ],
)
internal class TestKoinApplication

package koog.chat.core.sync.fake

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan(value = ["koog.chat.core.sync"])
class CoreSyncFakeModule

package koog.chat.core.auth.fake

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan(value = ["koog.chat.core.auth"])
class CoreAuthFakeModule

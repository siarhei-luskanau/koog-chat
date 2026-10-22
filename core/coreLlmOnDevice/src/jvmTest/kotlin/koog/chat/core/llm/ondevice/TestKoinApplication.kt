package koog.chat.core.llm.ondevice

import koog.chat.core.common.DispatcherSet
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal class TestProvidedModule {
    @Single
    fun dispatcherSet(): DispatcherSet = FakeDispatcherSet()
}

@KoinApplication(modules = [CoreLlmOnDeviceModule::class, TestProvidedModule::class])
internal class TestKoinApplication

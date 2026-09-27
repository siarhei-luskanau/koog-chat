package koog.chat.core.llm.koog

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.repository.ChatEntryRepository
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal class TestProvidedModule {
    @Single
    fun dispatcherSet(): DispatcherSet = FakeDispatcherSet()

    @Single
    fun chatEntryRepository(): ChatEntryRepository = FakeChatEntryRepository()
}

@KoinApplication(modules = [CoreLlmKoogModule::class, TestProvidedModule::class])
internal class TestKoinApplication

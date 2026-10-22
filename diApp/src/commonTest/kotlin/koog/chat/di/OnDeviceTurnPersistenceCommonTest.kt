package koog.chat.di

import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.database.api.repository.ChatRepository
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.LlmSessionManager
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

internal class OnDeviceTurnPersistenceCommonTest {
    @Test
    fun sendMessage_shouldPersistTurnWithNullLlmConfigId_whenBuiltinOnDeviceConfigUsedThroughRealGraph() =
        runTest {
            val koinApplication = koinApplication<DiKoinApplication>()
            koinApplication.koin.loadModules(
                listOf(module { single<OnDeviceLlm> { FakeOnDeviceLlm() } }),
                allowOverride = true,
            )
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val sessionManager = koinApplication.koin.get<LlmSessionManager>()
            val chat = Chat(id = "chat-${Random.nextLong()}", title = "On-device", createdAt = 1L)
            chatRepo.save(chat)

            try {
                val entries =
                    withContext(Dispatchers.Default) {
                        sessionManager.sendMessage(chat.id, "Hi", BUILTIN_CONFIG)
                        awaitTurnFinished(sessionManager, entryRepo, chat.id)
                    }

                assertEquals(
                    listOf(ChatEntryType.USER_PROMPT, ChatEntryType.SUCCESS_RESPONSE),
                    entries.map { it.type },
                )
                assertEquals("Hi", entries.first { it.type == ChatEntryType.USER_PROMPT }.content)
                assertEquals("Hello world", entries.first { it.type == ChatEntryType.SUCCESS_RESPONSE }.content)
                assertEquals(7L, entries.first { it.type == ChatEntryType.SUCCESS_RESPONSE }.tokensUsed)
                entries.forEach {
                    assertNull(it.llmConfigId)
                    assertEquals("OnDevice", it.llmProvider)
                    assertEquals("gemini-nano", it.llmModelId)
                }
            } finally {
                chatRepo.delete(chat.id)
                koinApplication.close()
            }
        }

    private class FakeOnDeviceLlm : OnDeviceLlm {
        override val availability: StateFlow<OnDeviceAvailability> = MutableStateFlow(OnDeviceAvailability.Available)
        override val maxContextTokens: Int = 4096

        override suspend fun refreshAvailability() = Unit

        override suspend fun download() = Unit

        override fun stream(
            history: List<ChatEntry>,
            prompt: String,
        ): Flow<LlmChunk> = flowOf(LlmChunk.Text("Hello"), LlmChunk.Text(" world\n"), LlmChunk.End(7L))
    }
}

internal val BUILTIN_CONFIG =
    LlmConfig(
        id = "builtin:on-device",
        provider = LlmProvider.OnDevice,
        modelId = "gemini-nano",
        apiKey = null,
        providerUrl = null,
        isDefault = false,
    )

internal suspend fun awaitTurnFinished(
    sessionManager: LlmSessionManager,
    entryRepo: ChatEntryRepository,
    chatId: String,
): List<ChatEntry> =
    withTimeout(30.seconds) {
        var entries = entryRepo.getAll(chatId)
        while (!entries.isTurnFinished() || chatId in sessionManager.activeChats.value) {
            delay(50)
            entries = entryRepo.getAll(chatId)
        }
        entries
    }

private fun List<ChatEntry>.isTurnFinished() = any { it.type == ChatEntryType.SUCCESS_RESPONSE || it.type == ChatEntryType.ERROR_RESPONSE }

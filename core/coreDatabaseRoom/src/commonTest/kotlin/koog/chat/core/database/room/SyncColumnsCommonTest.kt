package koog.chat.core.database.room

import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.database.api.repository.ChatRepository
import koog.chat.core.database.api.repository.LlmConfigRepository
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class SyncColumnsCommonTest {
    private fun uniqueId(prefix: String) = "$prefix-${Random.nextLong()}"

    @Test
    fun save_shouldDefaultSyncColumns_whenChatEntryAndConfigSaved() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val koin = koinApplication.koin
            val chatRepo = koin.get<ChatRepository>()
            val entryRepo = koin.get<ChatEntryRepository>()
            val configRepo = koin.get<LlmConfigRepository>()
            val database = koin.get<RoomDatabaseProvider>().database

            val chat = Chat(id = uniqueId("chat"), title = "Sync", createdAt = 1L)
            val config =
                LlmConfig(
                    id = uniqueId("config"),
                    provider = LlmProvider.Ollama,
                    modelId = "qwen3.5:0.8b",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = false,
                )
            chatRepo.save(chat)
            configRepo.save(config)
            val entry =
                ChatEntry(
                    id = uniqueId("entry"),
                    chatId = chat.id,
                    type = ChatEntryType.USER_PROMPT,
                    content = "Hello",
                    thinkingContent = null,
                    llmConfigId = config.id,
                    llmProvider = "Ollama",
                    llmModelId = "qwen3.5:0.8b",
                    tokensUsed = null,
                    tokensPerSecond = null,
                    responseTimeMs = null,
                    timestamp = 1L,
                )
            entryRepo.save(entry)

            val chatEntity = database.chatDao().getById(chat.id)!!
            val configEntity = database.llmConfigDao().getById(config.id)!!
            val entryEntity = database.chatEntryDao().getAll(chat.id).single()

            assertEquals(0L, chatEntity.updatedAt)
            assertFalse(chatEntity.isDirty)
            assertFalse(chatEntity.isDeleted)
            assertEquals(0L, configEntity.updatedAt)
            assertFalse(configEntity.isDirty)
            assertFalse(configEntity.isDeleted)
            assertEquals(0L, entryEntity.updatedAt)
            assertFalse(entryEntity.isDirty)
            assertFalse(entryEntity.isDeleted)

            chatRepo.delete(chat.id)
            configRepo.delete(config.id)
            koinApplication.close()
        }

    @Test
    fun save_shouldKeepSyncColumnDefaults_whenResavedThroughRepository() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val koin = koinApplication.koin
            val chatRepo = koin.get<ChatRepository>()
            val database = koin.get<RoomDatabaseProvider>().database

            val chat = Chat(id = uniqueId("chat"), title = "Original", createdAt = 1L)
            chatRepo.save(chat)
            chatRepo.save(chat.copy(title = "Updated"))

            val chatEntity = database.chatDao().getById(chat.id)!!

            assertEquals(0L, chatEntity.updatedAt)
            assertFalse(chatEntity.isDirty)
            assertFalse(chatEntity.isDeleted)

            chatRepo.delete(chat.id)
            koinApplication.close()
        }
}

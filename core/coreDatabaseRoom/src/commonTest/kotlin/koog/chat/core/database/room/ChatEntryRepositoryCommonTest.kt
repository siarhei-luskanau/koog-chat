package koog.chat.core.database.room

import androidx.paging.PagingSource
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChatEntryRepositoryCommonTest {
    private fun uniqueId(prefix: String) = "$prefix-${Random.nextLong()}"

    private fun testEntry(
        chatId: String,
        id: String = uniqueId("entry"),
        type: ChatEntryType = ChatEntryType.USER_PROMPT,
        content: String = "content",
        thinkingContent: String? = null,
        llmConfigId: String? = null,
        llmProvider: String = "Ollama",
        llmModelId: String = "qwen3.5:0.8b",
        tokensUsed: Long? = null,
        tokensPerSecond: Double? = null,
        responseTimeMs: Long? = null,
        timestamp: Long = 0L,
    ) = ChatEntry(
        id = id,
        chatId = chatId,
        type = type,
        content = content,
        thinkingContent = thinkingContent,
        llmConfigId = llmConfigId,
        llmProvider = llmProvider,
        llmModelId = llmModelId,
        tokensUsed = tokensUsed,
        tokensPerSecond = tokensPerSecond,
        responseTimeMs = responseTimeMs,
        timestamp = timestamp,
    )

    @Test
    fun save_shouldRoundTripAllFieldsOrderedByTimestampAsc_whenGetAllCalled() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "Round Trip", createdAt = 1L)
            chatRepo.save(chat)

            val prompt = testEntry(chatId = chat.id, type = ChatEntryType.USER_PROMPT, content = "Hello", timestamp = 1L)
            val thinking =
                testEntry(
                    chatId = chat.id,
                    type = ChatEntryType.THINKING,
                    content = "",
                    thinkingContent = "Let me think",
                    timestamp = 2L,
                )
            val success =
                testEntry(
                    chatId = chat.id,
                    type = ChatEntryType.SUCCESS_RESPONSE,
                    content = "Hi there",
                    llmProvider = "OpenAI",
                    llmModelId = "gpt-4o",
                    tokensUsed = 42L,
                    tokensPerSecond = 12.5,
                    responseTimeMs = 800L,
                    timestamp = 3L,
                )
            val error = testEntry(chatId = chat.id, type = ChatEntryType.ERROR_RESPONSE, content = "boom", timestamp = 4L)

            entryRepo.save(success)
            entryRepo.save(prompt)
            entryRepo.save(error)
            entryRepo.save(thinking)

            assertEquals(listOf(prompt, thinking, success, error), entryRepo.getAll(chat.id))

            chatRepo.delete(chat.id)
            koinApplication.close()
        }

    @Test
    fun update_shouldOverwriteExistingEntry_whenCalledWithSameId() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "Update", createdAt = 1L)
            chatRepo.save(chat)
            val entry = testEntry(chatId = chat.id, content = "original", timestamp = 1L)
            entryRepo.save(entry)

            val updated = entry.copy(content = "changed", tokensUsed = 10L)
            entryRepo.update(updated)

            assertEquals(listOf(updated), entryRepo.getAll(chat.id))

            chatRepo.delete(chat.id)
            koinApplication.close()
        }

    @Test
    fun delete_shouldCascadeToEntries_whenParentChatDeleted() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "Cascade", createdAt = 1L)
            chatRepo.save(chat)
            entryRepo.save(testEntry(chatId = chat.id, timestamp = 1L))

            chatRepo.delete(chat.id)

            assertTrue(entryRepo.getAll(chat.id).isEmpty())
            koinApplication.close()
        }

    @Test
    fun delete_shouldSetEntryLlmConfigIdToNull_whenReferencedConfigDeleted() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val configRepo = koinApplication.koin.get<LlmConfigRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "SetNull", createdAt = 1L)
            chatRepo.save(chat)
            val config =
                LlmConfig(
                    id = uniqueId("config"),
                    provider = LlmProvider.Ollama,
                    modelId = "qwen3.5:0.8b",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = false,
                )
            configRepo.save(config)
            entryRepo.save(testEntry(chatId = chat.id, llmConfigId = config.id, timestamp = 1L))

            configRepo.delete(config.id)

            assertNull(entryRepo.getAll(chat.id).single().llmConfigId)

            chatRepo.delete(chat.id)
            koinApplication.close()
        }

    @Test
    fun pagingSource_shouldReturnOnlyEntriesForGivenChat_whenRefreshLoaded() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val chatA = Chat(id = uniqueId("chat"), title = "A", createdAt = 1L)
            val chatB = Chat(id = uniqueId("chat"), title = "B", createdAt = 2L)
            chatRepo.save(chatA)
            chatRepo.save(chatB)
            val entryA = testEntry(chatId = chatA.id, timestamp = 1L)
            val entryB = testEntry(chatId = chatB.id, timestamp = 1L)
            entryRepo.save(entryA)
            entryRepo.save(entryB)

            val result =
                entryRepo.pagingSource(chatA.id).load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
                )
            val page = result as PagingSource.LoadResult.Page

            assertEquals(listOf(entryA), page.data)

            chatRepo.delete(chatA.id)
            chatRepo.delete(chatB.id)
            koinApplication.close()
        }
}

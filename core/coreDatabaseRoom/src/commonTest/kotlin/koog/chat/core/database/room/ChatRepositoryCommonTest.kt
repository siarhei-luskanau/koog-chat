package koog.chat.core.database.room

import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.repository.ChatRepository
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChatRepositoryCommonTest {
    @Test
    fun saveAndGetById() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = "test-1", title = "Test Chat", createdAt = 1000L)
            repo.save(chat)
            assertEquals(chat, repo.getById("test-1"))
            koinApplication.close()
        }

    @Test
    fun deleteById() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = "test-2", title = "To Delete", createdAt = 2000L)
            repo.save(chat)
            repo.delete("test-2")
            assertNull(repo.getById("test-2"))
            koinApplication.close()
        }

    @Test
    fun saveUpdatesExistingRecord() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = "test-3", title = "Original", createdAt = 3000L)
            repo.save(chat)
            val updated = chat.copy(title = "Updated")
            repo.save(updated)
            assertEquals(updated, repo.getById("test-3"))
            koinApplication.close()
        }
}

package koog.chat.core.database.room

import androidx.paging.PagingSource
import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.repository.ChatRepository
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChatRepositoryCommonTest {
    private fun uniqueId(prefix: String) = "$prefix-${Random.nextLong()}"

    @Test
    fun saveAndGetById_shouldReturnSavedChat() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "Test Chat", createdAt = 1000L)
            repo.save(chat)
            assertEquals(chat, repo.getById(chat.id))
            repo.delete(chat.id)
            koinApplication.close()
        }

    @Test
    fun deleteById_shouldRemoveChat() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "To Delete", createdAt = 2000L)
            repo.save(chat)
            repo.delete(chat.id)
            assertNull(repo.getById(chat.id))
            koinApplication.close()
        }

    @Test
    fun save_shouldUpdateExistingRecord_whenSavedAgain() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val chat = Chat(id = uniqueId("chat"), title = "Original", createdAt = 3000L)
            repo.save(chat)
            val updated = chat.copy(title = "Updated")
            repo.save(updated)
            assertEquals(updated, repo.getById(chat.id))
            repo.delete(chat.id)
            koinApplication.close()
        }

    @Test
    fun pagingSource_shouldReturnSavedChatsOrderedByCreatedAtDesc_whenRefreshLoaded() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<ChatRepository>()
            val base = 4_000_000_000_000_000L - Random.nextLong(from = 1L, until = 1_000_000_000L)
            val chat1 = Chat(id = uniqueId("chat"), title = "Chat 1", createdAt = base)
            val chat2 = Chat(id = uniqueId("chat"), title = "Chat 2", createdAt = base + 1)
            val chat3 = Chat(id = uniqueId("chat"), title = "Chat 3", createdAt = base + 2)
            repo.save(chat1)
            repo.save(chat3)
            repo.save(chat2)

            val result =
                repo.pagingSource().load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
                )
            val page = result as PagingSource.LoadResult.Page
            val ownIds = setOf(chat1.id, chat2.id, chat3.id)
            val ownChats = page.data.filter { it.id in ownIds }

            assertEquals(listOf(chat3, chat2, chat1), ownChats)

            repo.delete(chat1.id)
            repo.delete(chat2.id)
            repo.delete(chat3.id)
            koinApplication.close()
        }
}

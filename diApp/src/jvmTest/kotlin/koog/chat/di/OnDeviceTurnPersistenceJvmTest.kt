package koog.chat.di

import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.database.api.repository.ChatRepository
import koog.chat.core.llm.LlmSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.koin.plugin.module.dsl.koinApplication
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OnDeviceTurnPersistenceJvmTest {
    @Test
    fun sendMessage_shouldPersistErrorEntryWithNullLlmConfigId_whenRealJvmBindingIsUnavailable() =
        runTest {
            val koinApplication = koinApplication<DiKoinApplication>()
            val chatRepo = koinApplication.koin.get<ChatRepository>()
            val entryRepo = koinApplication.koin.get<ChatEntryRepository>()
            val sessionManager = koinApplication.koin.get<LlmSessionManager>()
            val chat = Chat(id = "chat-${Random.nextLong()}", title = "On-device JVM", createdAt = 1L)
            chatRepo.save(chat)

            try {
                val entries =
                    withContext(Dispatchers.Default) {
                        sessionManager.sendMessage(chat.id, "Hi", BUILTIN_CONFIG)
                        awaitTurnFinished(sessionManager, entryRepo, chat.id)
                    }

                assertEquals(
                    listOf(ChatEntryType.USER_PROMPT, ChatEntryType.ERROR_RESPONSE),
                    entries.map { it.type },
                )
                assertTrue(entries.none { it.type == ChatEntryType.SUCCESS_RESPONSE })
                assertTrue(
                    entries
                        .first { it.type == ChatEntryType.ERROR_RESPONSE }
                        .content
                        .contains("No on-device model on desktop JVM"),
                )
                entries.forEach {
                    assertNull(it.llmConfigId)
                    assertEquals("OnDevice", it.llmProvider)
                }
            } finally {
                chatRepo.delete(chat.id)
                koinApplication.close()
            }
        }
}

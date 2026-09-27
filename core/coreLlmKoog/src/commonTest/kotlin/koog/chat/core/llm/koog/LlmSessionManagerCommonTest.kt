package koog.chat.core.llm.koog

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.llm.ChatResult
import koog.chat.core.llm.LlmService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LlmSessionManagerCommonTest {
    @Test
    fun successfulTurnPersistsUserPromptAndFinalResponseForEveryProvider() =
        runTest {
            LlmProvider.entries.forEach { provider ->
                val chatId = "chat-${provider.name}"
                val repository =
                    FakeChatEntryRepository(
                        listOf(chatEntry(id = "old", chatId = chatId, type = ChatEntryType.USER_PROMPT, content = "earlier")),
                    )
                val llmService = FakeLlmService()
                val config = llmConfig(provider)

                sessionManager(repository, llmService).sendMessage(chatId = chatId, userText = "Hello", config = config)
                advanceUntilIdle()

                assertEquals(config, llmService.configs.single())
                assertEquals(listOf("earlier", "Hello"), llmService.messages.single().map { it.content })
                val (userEntry, responseEntry) = repository.entries.drop(1)
                assertEquals(ChatEntryType.USER_PROMPT, userEntry.type)
                assertEquals("Hello", userEntry.content)
                assertEntryAttributedTo(config, userEntry)
                assertEquals(ChatEntryType.SUCCESS_RESPONSE, responseEntry.type)
                assertEquals("Fine, thanks", responseEntry.content)
                assertEquals("hmm", responseEntry.thinkingContent)
                assertEquals(FakeLlmService.RESULT.tokensUsed, responseEntry.tokensUsed)
                assertEquals(FakeLlmService.RESULT.tokensPerSecond, responseEntry.tokensPerSecond)
                assertEquals(FakeLlmService.RESULT.responseTimeMs, responseEntry.responseTimeMs)
                assertEntryAttributedTo(config, responseEntry)
            }
        }

    @Test
    fun streamingPersistsPartialResponseBeforeCompletion() =
        runTest {
            val repository = FakeChatEntryRepository()

            sessionManager(repository, FakeLlmService()).sendMessage("chat", "Hello", llmConfig(LlmProvider.Ollama))
            advanceUntilIdle()

            val partial = repository.updates.first()
            assertEquals(ChatEntryType.THINKING, partial.type)
            assertEquals("hmm", partial.thinkingContent)
            assertEquals(ChatEntryType.SUCCESS_RESPONSE, repository.updates.last().type)
        }

    @Test
    fun failedTurnPersistsErrorResponseForEveryProvider() =
        runTest {
            LlmProvider.entries.forEach { provider ->
                val repository = FakeChatEntryRepository()
                val llmService = FakeLlmService(failure = IllegalStateException("$provider unavailable"))
                val manager = sessionManager(repository, llmService)

                manager.sendMessage("chat", "Hello", llmConfig(provider))
                advanceUntilIdle()

                val responseEntry = repository.entries.last()
                assertEquals(ChatEntryType.ERROR_RESPONSE, responseEntry.type)
                assertEquals("$provider unavailable", responseEntry.content)
                assertNull(responseEntry.thinkingContent)
                assertTrue(manager.activeChats.value.isEmpty())
            }
        }

    @Test
    fun repositoryFailureIsReportedAsErrorResponse() =
        runTest {
            val repository = FakeChatEntryRepository().apply { failOnGetAll = IllegalStateException("db closed") }
            val manager = sessionManager(repository, FakeLlmService())

            manager.sendMessage("chat", "Hello", llmConfig(LlmProvider.Ollama))
            advanceUntilIdle()

            assertTrue(repository.updates.isEmpty())
            val errorEntry = repository.entries.single()
            assertEquals(ChatEntryType.ERROR_RESPONSE, errorEntry.type)
            assertEquals("db closed", errorEntry.content)
            assertTrue(manager.activeChats.value.isEmpty())
        }

    @Test
    fun errorResponseNeverPersistsTheApiKey() =
        runTest {
            val repository = FakeChatEntryRepository()
            val config = llmConfig(LlmProvider.Google, apiKey = "secret-key-123")
            val failure =
                IllegalStateException(
                    "Request timeout has expired [url=https://generativelanguage.googleapis.com/v1beta/models/m" +
                        ":streamGenerateContent?alt=sse&key=secret-key-123, request_timeout=unknown ms]",
                )

            sessionManager(repository, FakeLlmService(failure = failure)).sendMessage("chat", "Hello", config)
            advanceUntilIdle()

            val errorEntry = repository.entries.last()
            assertEquals(ChatEntryType.ERROR_RESPONSE, errorEntry.type)
            assertFalse(errorEntry.content.contains("secret-key-123"), errorEntry.content)
            assertTrue(errorEntry.content.contains("key=***"), errorEntry.content)
        }

    @Test
    fun errorResponseRedactsKeyQueryParamEvenWithoutConfiguredKey() =
        runTest {
            val repository = FakeChatEntryRepository()
            val failure = IllegalStateException("failed [url=https://host/path?alt=sse&key=other-key]")

            sessionManager(repository, FakeLlmService(failure = failure))
                .sendMessage("chat", "Hello", llmConfig(LlmProvider.Ollama, apiKey = null))
            advanceUntilIdle()

            assertEquals("failed [url=https://host/path?alt=sse&key=***]", repository.entries.last().content)
        }

    @Test
    fun isGeneratingTracksTheActiveTurnPerChat() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val manager = sessionManager(FakeChatEntryRepository(), FakeLlmService(gate = gate))

            manager.sendMessage("chat", "Hello", llmConfig(LlmProvider.Ollama))
            assertTrue(manager.isGenerating("chat").first())
            advanceUntilIdle()
            assertTrue(manager.isGenerating("chat").first())
            assertFalse(manager.isGenerating("other").first())

            gate.complete(Unit)
            advanceUntilIdle()
            assertFalse(manager.isGenerating("chat").first())
            assertEquals(emptySet(), manager.activeChats.value)
        }

    private fun TestScope.sessionManager(
        repository: FakeChatEntryRepository,
        llmService: LlmService,
    ) = LlmSessionManagerImpl(
        chatEntryRepository = repository,
        llmService = llmService,
        dispatcherSet = FakeDispatcherSet(default = StandardTestDispatcher(testScheduler)),
    )

    private fun assertEntryAttributedTo(
        config: LlmConfig,
        entry: ChatEntry,
    ) {
        assertEquals(config.id, entry.llmConfigId)
        assertEquals(config.provider.name, entry.llmProvider)
        assertEquals(config.modelId, entry.llmModelId)
    }

    private class FakeLlmService(
        private val failure: Exception? = null,
        private val gate: CompletableDeferred<Unit>? = null,
    ) : LlmService {
        val configs = mutableListOf<LlmConfig>()
        val messages = mutableListOf<List<ChatEntry>>()

        override suspend fun chat(
            messages: List<ChatEntry>,
            config: LlmConfig,
            onThinkingChunk: suspend (String) -> Unit,
            onTextChunk: suspend (String) -> Unit,
        ): ChatResult {
            configs += config
            this.messages += messages
            gate?.await()
            failure?.let { throw it }
            onThinkingChunk("hmm")
            onTextChunk("Fine, ")
            onTextChunk("thanks")
            return RESULT
        }

        companion object {
            val RESULT =
                ChatResult(
                    content = "Fine, thanks",
                    thinkingContent = "hmm",
                    tokensUsed = 7L,
                    tokensPerSecond = 3.5,
                    responseTimeMs = 2000L,
                )
        }
    }
}

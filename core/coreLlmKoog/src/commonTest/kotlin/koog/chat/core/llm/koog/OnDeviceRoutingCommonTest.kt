package koog.chat.core.llm.koog

import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.llm.ChatResult
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OnDeviceRoutingCommonTest {
    private val engine = RecordingMockEngine { respondError(HttpStatusCode.InternalServerError) }
    private val thinkingChunks = mutableListOf<String>()
    private val textChunks = mutableListOf<String>()

    private val messages =
        listOf(
            chatEntry(id = "1", chatId = "chat", type = ChatEntryType.USER_PROMPT, content = "Hi"),
            chatEntry(id = "2", chatId = "chat", type = ChatEntryType.SUCCESS_RESPONSE, content = "Hello!"),
            chatEntry(id = "3", chatId = "chat", type = ChatEntryType.ERROR_RESPONSE, content = "boom"),
            chatEntry(id = "4", chatId = "chat", type = ChatEntryType.THINKING, content = "partial"),
            chatEntry(id = "5", chatId = "chat", type = ChatEntryType.USER_PROMPT, content = "How are you?"),
        )

    @Test
    fun onDeviceConfigRoutesToOnDeviceLlmWithoutHttpRequest() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm()

            chat(onDeviceLlm)

            assertEquals(1, onDeviceLlm.streamCalls.size)
            assertTrue(engine.requests.isEmpty())
        }

    @Test
    fun chunksMapToCallbacksAndResult() =
        runTest {
            val onDeviceLlm =
                FakeOnDeviceLlm(
                    chunks =
                        listOf(
                            LlmChunk.Reasoning("Let me think"),
                            LlmChunk.Text("Fine, "),
                            LlmChunk.Text(""),
                            LlmChunk.Text("thanks \n"),
                            LlmChunk.End(tokensUsed = 7L),
                        ),
                )

            val result = chat(onDeviceLlm)

            assertEquals("Fine, thanks", result.content)
            assertEquals("Let me think", result.thinkingContent)
            assertEquals(7L, result.tokensUsed)
            assertEquals(listOf("Fine, ", "thanks \n"), textChunks)
            assertEquals(listOf("Let me think"), thinkingChunks)
        }

    @Test
    fun missingReasoningAndTokensYieldNulls() =
        runTest {
            val result = chat(FakeOnDeviceLlm(chunks = listOf(LlmChunk.Text("Hi"), LlmChunk.End(tokensUsed = null))))

            assertNull(result.thinkingContent)
            assertNull(result.tokensUsed)
            assertNull(result.tokensPerSecond)
        }

    @Test
    fun historyExcludesThinkingAndErrorEntriesAndPromptIsLastUserEntry() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm()

            chat(onDeviceLlm)

            val (history, prompt) = onDeviceLlm.streamCalls.single()
            assertEquals(listOf("1", "2"), history.map { it.id })
            assertEquals("How are you?", prompt)
        }

    @Test
    fun historyIsTrimmedToTheContextWindow() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm(maxContextTokens = 4)

            chat(onDeviceLlm)

            val (history, prompt) = onDeviceLlm.streamCalls.single()
            assertEquals("How are you?", prompt)
            assertTrue(history.isEmpty())
        }

    @Test
    fun unavailableModelFailsWithReason() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm(availability = OnDeviceAvailability.Unavailable("no AICore"))

            val error = assertFailsWith<IllegalStateException> { chat(onDeviceLlm) }

            assertTrue(error.message.orEmpty().contains("no AICore"), error.message)
            assertTrue(onDeviceLlm.streamCalls.isEmpty())
        }

    @Test
    fun downloadableModelFails() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm(availability = OnDeviceAvailability.Downloadable)

            assertFailsWith<IllegalStateException> { chat(onDeviceLlm) }

            assertTrue(onDeviceLlm.streamCalls.isEmpty())
        }

    @Test
    fun streamFailurePropagates() =
        runTest {
            val onDeviceLlm = FakeOnDeviceLlm(chunks = emptyList(), failure = IllegalArgumentException("model crashed"))

            val error = assertFailsWith<IllegalArgumentException> { chat(onDeviceLlm) }

            assertEquals("model crashed", error.message)
        }

    private suspend fun chat(onDeviceLlm: FakeOnDeviceLlm): ChatResult =
        LlmServiceKoog(
            llmClientFactory = LlmClientFactory(engine.httpClientFactory),
            dispatcherSet = platformIoDispatcherSet(),
            onDeviceLlm = onDeviceLlm,
        ).chat(
            messages = messages,
            config = llmConfig(LlmProvider.OnDevice, apiKey = null),
            onThinkingChunk = { thinkingChunks += it },
            onTextChunk = { textChunks += it },
        )
}

package koog.chat.core.llm.koog

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.llm.ChatResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LlmServiceKoogCommonTest {
    private val history =
        listOf(
            chatEntry(id = "1", chatId = "chat", type = ChatEntryType.USER_PROMPT, content = "Hi"),
            chatEntry(id = "2", chatId = "chat", type = ChatEntryType.SUCCESS_RESPONSE, content = "Hello!"),
            chatEntry(id = "3", chatId = "chat", type = ChatEntryType.ERROR_RESPONSE, content = "boom"),
            chatEntry(id = "4", chatId = "chat", type = ChatEntryType.USER_PROMPT, content = "How are you?"),
        )

    @Test
    fun ollamaStreamsTextAndThinking() =
        runTest {
            val engine =
                RecordingMockEngine {
                    respondStream(
                        contentType = "application/x-ndjson",
                        body =
                            """
                            {"model":"m","message":{"role":"assistant","content":"","thinking":"Let me think"},"done":false}
                            {"model":"m","message":{"role":"assistant","content":"Fine, "},"done":false}
                            {"model":"m","message":{"role":"assistant","content":"thanks"},"done":false}
                            {"model":"m","message":{"role":"assistant","content":""},"done":true,"prompt_eval_count":3,"eval_count":4}
                            """.trimIndent() + "\n",
                    )
                }
            val chunks = Chunks()

            val result = chat(engine, llmConfig(LlmProvider.Ollama, apiKey = null, providerUrl = "http://ollama.test:1234"), chunks)

            assertEquals("Fine, thanks", result.content)
            assertEquals("Let me think", result.thinkingContent)
            assertEquals(listOf("Fine, ", "thanks"), chunks.text)
            assertEquals(listOf("Let me think"), chunks.thinking)
            val request = engine.requests.single()
            assertEquals("http://ollama.test:1234/api/chat", request.url.toString())
            assertRequestCarriesHistory(request.bodyText())
        }

    @Test
    fun openAiStreamsTextAndReportsTokens() =
        runTest {
            val engine =
                RecordingMockEngine {
                    respondSse(
                        openAiChunk("""{"role":"assistant","content":"Fine, "}""", finishReason = null),
                        openAiChunk("""{"content":"thanks"}""", finishReason = null),
                        openAiChunk("{}", finishReason = "\"stop\""),
                        """{"id":"c","object":"chat.completion.chunk","created":1,"model":"m","choices":[],""" +
                            """"usage":{"prompt_tokens":3,"completion_tokens":4,"total_tokens":7}}""",
                        "[DONE]",
                    )
                }
            val chunks = Chunks()

            val result = chat(engine, llmConfig(LlmProvider.OpenAI), chunks)

            assertEquals("Fine, thanks", result.content)
            assertNull(result.thinkingContent)
            assertEquals(7L, result.tokensUsed)
            assertEquals(listOf("Fine, ", "thanks"), chunks.text)
            val request = engine.requests.single()
            assertEquals("https://api.openai.com/v1/chat/completions", request.url.toString())
            assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])
            assertRequestCarriesHistory(request.bodyText())
        }

    @Test
    fun anthropicStreamsTextAndThinking() =
        runTest {
            val engine =
                RecordingMockEngine {
                    respondSse(
                        """{"type":"message_start","message":{"id":"msg","type":"message","role":"assistant",""" +
                            """"content":[],"model":"m","stop_reason":null,"stop_sequence":null,""" +
                            """"usage":{"input_tokens":3,"output_tokens":1}}}""",
                        """{"type":"content_block_start","index":0,"content_block":{"type":"thinking","thinking":"","signature":""}}""",
                        """{"type":"content_block_delta","index":0,"delta":{"type":"thinking_delta","thinking":"Let me think"}}""",
                        """{"type":"content_block_stop","index":0}""",
                        """{"type":"content_block_start","index":1,"content_block":{"type":"text","text":""}}""",
                        """{"type":"content_block_delta","index":1,"delta":{"type":"text_delta","text":"Fine, "}}""",
                        """{"type":"content_block_delta","index":1,"delta":{"type":"text_delta","text":"thanks"}}""",
                        """{"type":"content_block_stop","index":1}""",
                        """{"type":"message_delta","delta":{"stop_reason":"end_turn","stop_sequence":null},"usage":{"output_tokens":4}}""",
                        """{"type":"message_stop"}""",
                        eventNames = true,
                    )
                }
            val chunks = Chunks()

            val result = chat(engine, llmConfig(LlmProvider.Anthropic, providerUrl = "https://anthropic.test"), chunks)

            assertEquals("Fine, thanks", result.content)
            assertEquals("Let me think", result.thinkingContent)
            assertEquals(listOf("Fine, ", "thanks"), chunks.text)
            val request = engine.requests.single()
            assertEquals("https://anthropic.test/v1/messages", request.url.toString())
            assertEquals("test-key", request.headers["x-api-key"])
            assertTrue(request.bodyText().contains("\"model-anthropic\""))
            assertRequestCarriesHistory(request.bodyText())
        }

    @Test
    fun googleStreamsTextAndReportsTokens() =
        runTest {
            val engine =
                RecordingMockEngine {
                    respondSse(
                        """{"candidates":[{"content":{"parts":[{"text":"Fine, "}],"role":"model"},"index":0}]}""",
                        """{"candidates":[{"content":{"parts":[{"text":"thanks"}],"role":"model"},""" +
                            """"finishReason":"STOP","index":0}],""" +
                            """"usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":4,"totalTokenCount":7}}""",
                    )
                }
            val chunks = Chunks()

            val result = chat(engine, llmConfig(LlmProvider.Google), chunks)

            assertEquals("Fine, thanks", result.content)
            assertEquals(7L, result.tokensUsed)
            assertEquals(listOf("Fine, ", "thanks"), chunks.text)
            val request = engine.requests.single()
            assertEquals("generativelanguage.googleapis.com", request.url.host)
            assertEquals("/v1beta/models/model-google:streamGenerateContent", request.url.encodedPath)
            assertEquals("test-key", request.url.parameters["key"])
            assertEquals("sse", request.url.parameters["alt"])
            assertRequestCarriesHistory(request.bodyText())
        }

    @Test
    fun httpErrorFailsTheCall() =
        runTest {
            LlmProvider.entries.forEach { provider ->
                val engine = RecordingMockEngine { respondError(HttpStatusCode.Unauthorized, "denied") }
                assertFails("$provider must fail on HTTP 401") {
                    chat(engine, llmConfig(provider), Chunks())
                }
            }
        }

    private suspend fun chat(
        engine: RecordingMockEngine,
        config: LlmConfig,
        chunks: Chunks,
    ): ChatResult =
        LlmServiceKoog(
            llmClientFactory = LlmClientFactory(engine.httpClientFactory),
            dispatcherSet = platformIoDispatcherSet(),
        ).chat(
            messages = history,
            config = config,
            onThinkingChunk = { chunks.thinking += it },
            onTextChunk = { chunks.text += it },
        )

    private fun assertRequestCarriesHistory(body: String) {
        assertTrue(body.contains("You are a helpful assistant."), body)
        assertTrue(body.contains("Hi"), body)
        assertTrue(body.contains("Hello!"), body)
        assertTrue(body.contains("How are you?"), body)
        assertTrue(!body.contains("boom"), "error entries must not be sent to the model: $body")
    }

    private class Chunks {
        val text = mutableListOf<String>()
        val thinking = mutableListOf<String>()
    }

    private fun openAiChunk(
        delta: String,
        finishReason: String?,
    ) = """{"id":"c","object":"chat.completion.chunk","created":1,"model":"m",""" +
        """"choices":[{"index":0,"delta":$delta,"finish_reason":$finishReason}]}"""

    private fun MockRequestHandleScope.respondSse(
        vararg events: String,
        eventNames: Boolean = false,
    ) = respondStream(
        contentType = "text/event-stream",
        body =
            events.joinToString(separator = "") { data ->
                val eventLine =
                    if (eventNames) {
                        "event: " + Regex("\"type\":\"([a-z_]+)\"").find(data)!!.groupValues[1] + "\n"
                    } else {
                        ""
                    }
                "${eventLine}data: $data\n\n"
            },
    )

    private fun MockRequestHandleScope.respondStream(
        contentType: String,
        body: String,
    ) = respond(
        content = ByteReadChannel(body),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, contentType),
    )

    private fun HttpRequestData.bodyText(): String {
        val content = body.let { if (it is OutgoingContent.ContentWrapper) it.delegate() else it }
        return (content as OutgoingContent.ByteArrayContent).bytes().decodeToString()
    }
}

package koog.chat.core.llm.koog

import ai.koog.http.client.ktor.KtorKoogHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

internal class OllamaStreamingCommonTest {
    @Test
    fun streamsRealResponseFromLocalOllamaOnEveryTarget() =
        runTest(timeout = 3.minutes) {
            val httpClient = HttpClient()
            val tags = runCatching { httpClient.get("$OLLAMA_URL/api/tags").bodyAsText() }.getOrNull()
            if (tags == null || "\"$MODEL_ID\"" !in tags) {
                println("Skipping: $MODEL_ID not available at $OLLAMA_URL")
                httpClient.close()
                return@runTest
            }
            val service =
                LlmServiceKoog(
                    llmClientFactory = LlmClientFactory(KtorKoogHttpClient.Factory(httpClient)),
                    dispatcherSet = platformIoDispatcherSet(),
                )
            val textChunks = mutableListOf<String>()

            val result =
                service.chat(
                    messages = listOf(chatEntry(id = "1", chatId = "chat", type = ChatEntryType.USER_PROMPT, content = "Say hi")),
                    config = llmConfig(LlmProvider.Ollama, apiKey = null, providerUrl = OLLAMA_URL).copy(modelId = MODEL_ID),
                    onThinkingChunk = {},
                    onTextChunk = { textChunks += it },
                )

            assertTrue(result.content.isNotBlank(), "empty response from $MODEL_ID")
            assertTrue(textChunks.isNotEmpty(), "response was not streamed")
            httpClient.close()
        }

    private companion object {
        const val OLLAMA_URL = "http://localhost:11434"
        const val MODEL_ID = "qwen3.5:0.8b"
    }
}

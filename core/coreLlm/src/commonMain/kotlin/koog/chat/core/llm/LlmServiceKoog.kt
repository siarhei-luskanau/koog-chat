package koog.chat.core.llm

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.streaming.StreamFrame
import io.ktor.client.HttpClient
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
internal class LlmServiceKoog : LlmService {
    override suspend fun chat(
        messages: List<ChatEntry>,
        config: LlmConfig,
        onThinkingChunk: suspend (String) -> Unit,
        onTextChunk: suspend (String) -> Unit,
    ): ChatResult {
        val client =
            OllamaClient(
                httpClientFactory = KtorKoogHttpClient.Factory(HttpClient()),
                baseUrl = config.providerUrl ?: DEFAULT_OLLAMA_URL,
            )
        val startTime = Clock.System.now().toEpochMilliseconds()
        val textBuilder = StringBuilder()
        val thinkingBuilder = StringBuilder()
        var totalTokens: Long? = null

        try {
            val koogPrompt = buildKoogPrompt(messages)
            val koogModel = buildKoogModel(config)
            client.executeStreaming(koogPrompt, koogModel).collect { frame ->
                when (frame) {
                    is StreamFrame.TextDelta -> {
                        textBuilder.append(frame.text)
                        onTextChunk(frame.text)
                    }

                    is StreamFrame.ReasoningDelta -> {
                        val chunk = frame.text ?: return@collect
                        thinkingBuilder.append(chunk)
                        onThinkingChunk(chunk)
                    }

                    is StreamFrame.End -> {
                        totalTokens = frame.metaInfo.totalTokensCount?.toLong()
                    }

                    else -> {
                        Unit
                    }
                }
            }
        } finally {
            client.close()
        }

        val responseTimeMs = Clock.System.now().toEpochMilliseconds() - startTime
        val tokensPerSecond = computeTokensPerSecond(totalTokens, responseTimeMs)
        val thinkingContent = thinkingBuilder.toString().takeIf { it.isNotEmpty() }

        return ChatResult(
            content = textBuilder.toString(),
            thinkingContent = thinkingContent,
            tokensUsed = totalTokens,
            tokensPerSecond = tokensPerSecond,
            responseTimeMs = responseTimeMs,
        )
    }

    private fun buildKoogPrompt(messages: List<ChatEntry>) =
        prompt("chat") {
            system(SYSTEM_PROMPT)
            messages.forEach { entry ->
                when (entry.type) {
                    ChatEntryType.USER_PROMPT -> user(entry.content)
                    ChatEntryType.SUCCESS_RESPONSE -> assistant(entry.content)
                    ChatEntryType.THINKING -> Unit
                    ChatEntryType.ERROR_RESPONSE -> Unit
                }
            }
        }

    private fun buildKoogModel(config: LlmConfig) =
        LLModel(
            provider = LLMProvider.Ollama,
            id = config.modelId,
            capabilities = listOf(LLMCapability.Thinking),
            contextLength = DEFAULT_CONTEXT_LENGTH,
        )

    private fun computeTokensPerSecond(
        totalTokens: Long?,
        responseTimeMs: Long,
    ): Double? {
        if (totalTokens == null || totalTokens <= 0L || responseTimeMs <= 0L) return null
        return totalTokens.toDouble() * MILLIS_PER_SECOND / responseTimeMs.toDouble()
    }

    private companion object {
        const val DEFAULT_OLLAMA_URL = "http://localhost:11434"
        const val SYSTEM_PROMPT = "You are a helpful assistant."
        const val DEFAULT_CONTEXT_LENGTH = 256_000L
        const val MILLIS_PER_SECOND = 1000.0
    }
}

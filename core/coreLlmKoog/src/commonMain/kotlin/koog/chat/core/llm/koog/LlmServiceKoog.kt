package koog.chat.core.llm.koog

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.streaming.StreamFrame
import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.llm.ChatResult
import koog.chat.core.llm.LlmService
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
internal class LlmServiceKoog(
    private val llmClientFactory: LlmClientFactory,
    @Provided private val dispatcherSet: DispatcherSet,
) : LlmService {
    override suspend fun chat(
        messages: List<ChatEntry>,
        config: LlmConfig,
        onThinkingChunk: suspend (String) -> Unit,
        onTextChunk: suspend (String) -> Unit,
    ): ChatResult {
        val model = llmClientFactory.createModel(config)
        val client = llmClientFactory.createClient(config, model)
        val startTime = Clock.System.now().toEpochMilliseconds()
        val textBuilder = StringBuilder()
        val thinkingBuilder = StringBuilder()
        var totalTokens: Long? = null

        try {
            // Koog emits frames off the collector's context on iOS/JS/WasmJs (docs/DECISIONS.md, row 3 spike)
            client
                .executeStreaming(buildKoogPrompt(messages), model)
                .flowOn(dispatcherSet.ioDispatcher())
                .collect { frame ->
                    when (frame) {
                        is StreamFrame.TextDelta -> {
                            if (frame.text.isEmpty()) return@collect
                            textBuilder.append(frame.text)
                            onTextChunk(frame.text)
                        }

                        is StreamFrame.ReasoningDelta -> {
                            val chunk = frame.text?.takeIf { it.isNotEmpty() } ?: return@collect
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
        return ChatResult(
            content = textBuilder.toString(),
            thinkingContent = thinkingBuilder.toString().takeIf { it.isNotEmpty() },
            tokensUsed = totalTokens,
            tokensPerSecond = computeTokensPerSecond(totalTokens, responseTimeMs),
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

    private fun computeTokensPerSecond(
        totalTokens: Long?,
        responseTimeMs: Long,
    ): Double? {
        if (totalTokens == null || totalTokens <= 0L || responseTimeMs <= 0L) return null
        return totalTokens.toDouble() * MILLIS_PER_SECOND / responseTimeMs.toDouble()
    }

    private companion object {
        const val SYSTEM_PROMPT = "You are a helpful assistant."
        const val MILLIS_PER_SECOND = 1000.0
    }
}

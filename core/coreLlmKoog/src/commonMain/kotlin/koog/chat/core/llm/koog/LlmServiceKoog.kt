package koog.chat.core.llm.koog

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.streaming.StreamFrame
import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.llm.ChatResult
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.LlmService
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
internal class LlmServiceKoog(
    private val llmClientFactory: LlmClientFactory,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val onDeviceLlm: OnDeviceLlm,
) : LlmService {
    override suspend fun chat(
        messages: List<ChatEntry>,
        config: LlmConfig,
        onThinkingChunk: suspend (String) -> Unit,
        onTextChunk: suspend (String) -> Unit,
    ): ChatResult {
        val collector = ResponseCollector(onThinkingChunk = onThinkingChunk, onTextChunk = onTextChunk)
        return if (config.provider == LlmProvider.OnDevice) {
            chatOnDevice(messages, collector)
        } else {
            chatWithKoog(messages, config, collector)
        }
    }

    private suspend fun chatWithKoog(
        messages: List<ChatEntry>,
        config: LlmConfig,
        collector: ResponseCollector,
    ): ChatResult {
        val model = llmClientFactory.createModel(config)
        val client = llmClientFactory.createClient(config, model)
        val startTime = Clock.System.now().toEpochMilliseconds()

        try {
            // Koog emits frames off the collector's context on iOS/JS/WasmJs (docs/DECISIONS.md, row 3 spike)
            client
                .executeStreaming(buildKoogPrompt(messages), model)
                .flowOn(dispatcherSet.ioDispatcher())
                .collect { frame ->
                    when (frame) {
                        is StreamFrame.TextDelta -> {
                            collector.appendText(frame.text)
                        }

                        is StreamFrame.ReasoningDelta -> {
                            collector.appendThinking(frame.text.orEmpty())
                        }

                        is StreamFrame.End -> {
                            collector.tokensUsed = frame.metaInfo.totalTokensCount?.toLong()
                        }

                        else -> {
                            Unit
                        }
                    }
                }
        } finally {
            client.close()
        }

        return buildResult(collector, startTime, collector.text.toString())
    }

    private suspend fun chatOnDevice(
        messages: List<ChatEntry>,
        collector: ResponseCollector,
    ): ChatResult {
        check(onDeviceLlm.availability.value is OnDeviceAvailability.Available) {
            "On-device model is not available: ${describe(onDeviceLlm.availability.value)}"
        }
        val conversation = messages.filter { it.type in CONVERSATION_TYPES }
        val prompt = checkNotNull(conversation.lastOrNull()) { "No user prompt to send to the on-device model" }.content
        val history =
            trimToContext(
                history = conversation.dropLast(1),
                prompt = prompt,
                maxContextTokens = onDeviceLlm.maxContextTokens,
            )
        val startTime = Clock.System.now().toEpochMilliseconds()

        onDeviceLlm.stream(history, prompt).collect { chunk ->
            when (chunk) {
                is LlmChunk.Text -> collector.appendText(chunk.text)
                is LlmChunk.Reasoning -> collector.appendThinking(chunk.text)
                is LlmChunk.End -> collector.tokensUsed = chunk.tokensUsed
            }
        }

        return buildResult(collector, startTime, collector.text.toString().trimEnd())
    }

    private fun describe(availability: OnDeviceAvailability) =
        when (availability) {
            is OnDeviceAvailability.Unavailable -> availability.reason
            else -> availability.toString()
        }

    private fun buildResult(
        collector: ResponseCollector,
        startTime: Long,
        content: String,
    ): ChatResult {
        val responseTimeMs = Clock.System.now().toEpochMilliseconds() - startTime
        return ChatResult(
            content = content,
            thinkingContent = collector.thinking.toString().takeIf { it.isNotEmpty() },
            tokensUsed = collector.tokensUsed,
            tokensPerSecond = computeTokensPerSecond(collector.tokensUsed, responseTimeMs),
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
        val CONVERSATION_TYPES = setOf(ChatEntryType.USER_PROMPT, ChatEntryType.SUCCESS_RESPONSE)
    }
}

private class ResponseCollector(
    private val onThinkingChunk: suspend (String) -> Unit,
    private val onTextChunk: suspend (String) -> Unit,
) {
    val text = StringBuilder()
    val thinking = StringBuilder()
    var tokensUsed: Long? = null

    suspend fun appendText(chunk: String) {
        if (chunk.isEmpty()) return
        text.append(chunk)
        onTextChunk(chunk)
    }

    suspend fun appendThinking(chunk: String) {
        if (chunk.isEmpty()) return
        thinking.append(chunk)
        onThinkingChunk(chunk)
    }
}

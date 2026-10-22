package koog.chat.core.llm.ondevice

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class FakeDispatcherSet(
    private val default: CoroutineDispatcher = Dispatchers.Default,
    private val io: CoroutineDispatcher = Dispatchers.Default,
) : DispatcherSet {
    override fun defaultDispatcher() = default

    override fun ioDispatcher() = io

    override fun mainDispatcher() = default
}

internal fun chatEntry(
    type: ChatEntryType,
    content: String,
) = ChatEntry(
    id = "id-$content",
    chatId = "chat",
    type = type,
    content = content,
    thinkingContent = null,
    llmConfigId = null,
    llmProvider = "provider",
    llmModelId = "model",
    tokensUsed = null,
    tokensPerSecond = null,
    responseTimeMs = null,
    timestamp = 0L,
)

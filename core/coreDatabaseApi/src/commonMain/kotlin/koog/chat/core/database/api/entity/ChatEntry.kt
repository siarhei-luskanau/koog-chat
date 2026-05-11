package koog.chat.core.database.api.entity

data class ChatEntry(
    val id: String,
    val chatId: String,
    val type: ChatEntryType,
    val content: String,
    val thinkingContent: String?,
    val llmConfigId: String?,
    val llmProvider: String,
    val llmModelId: String,
    val tokensUsed: Long?,
    val tokensPerSecond: Double?,
    val responseTimeMs: Long?,
    val timestamp: Long,
)

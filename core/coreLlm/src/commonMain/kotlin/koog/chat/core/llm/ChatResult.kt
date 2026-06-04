package koog.chat.core.llm

data class ChatResult(
    val content: String,
    val thinkingContent: String?,
    val tokensUsed: Long?,
    val tokensPerSecond: Double?,
    val responseTimeMs: Long?,
)

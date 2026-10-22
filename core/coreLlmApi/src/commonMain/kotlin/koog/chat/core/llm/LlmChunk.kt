package koog.chat.core.llm

sealed interface LlmChunk {
    data class Text(
        val text: String,
    ) : LlmChunk

    data class Reasoning(
        val text: String,
    ) : LlmChunk

    data class End(
        val tokensUsed: Long?,
    ) : LlmChunk
}

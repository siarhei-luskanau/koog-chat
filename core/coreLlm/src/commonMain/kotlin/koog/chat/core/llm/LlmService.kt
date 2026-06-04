package koog.chat.core.llm

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.LlmConfig

interface LlmService {
    suspend fun chat(
        messages: List<ChatEntry>,
        config: LlmConfig,
        onThinkingChunk: suspend (String) -> Unit,
        onTextChunk: suspend (String) -> Unit,
    ): ChatResult
}

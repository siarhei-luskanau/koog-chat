package koog.chat.core.llm

import koog.chat.core.database.api.entity.LlmConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LlmSessionManager {
    val activeChats: StateFlow<Set<String>>

    fun isGenerating(chatId: String): Flow<Boolean>

    fun sendMessage(
        chatId: String,
        userText: String,
        config: LlmConfig,
    )
}

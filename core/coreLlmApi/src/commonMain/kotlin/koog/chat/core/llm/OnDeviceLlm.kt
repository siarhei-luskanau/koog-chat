package koog.chat.core.llm

import koog.chat.core.database.api.entity.ChatEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OnDeviceLlm {
    val availability: StateFlow<OnDeviceAvailability>
    val maxContextTokens: Int

    suspend fun refreshAvailability()

    suspend fun download()

    fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk>
}

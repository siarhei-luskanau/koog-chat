package koog.chat.ui.chat

import androidx.compose.runtime.Immutable
import koog.chat.ui.common.components.Metrics

sealed interface ChatViewState {
    data object Loading : ChatViewState

    data class Success(
        val chatTitle: String = "Chat",
        val messages: List<ChatMessage> = emptyList(),
        val inputText: String = "",
        val isGenerating: Boolean = false,
        val selectedModel: String = "qwen3.5:0.8b",
        val isAdvancedMode: Boolean,
        val totalTokens: Int?,
    ) : ChatViewState

    data class Error(
        val error: Throwable,
    ) : ChatViewState
}

enum class ChatMessageType {
    USER,
    THINKING,
    ASSISTANT,
    ERROR,
}

@Immutable
data class ChatMessage(
    val id: String,
    val type: ChatMessageType,
    val content: String,
    val isStreaming: Boolean = false,
    val metrics: Metrics? = null,
)

package koog.chat.ui.chat

sealed interface ChatViewState {
    data object Loading : ChatViewState

    data class Success(
        val chatTitle: String = "Chat",
        val inputText: String = "",
        val isGenerating: Boolean = false,
        val selectedModel: String = "qwen3.5:0.8b",
        val selectedConfigId: String?,
        val isAdvancedMode: Boolean,
        val totalTokens: Int?,
    ) : ChatViewState

    data class Error(
        val error: Throwable,
    ) : ChatViewState
}

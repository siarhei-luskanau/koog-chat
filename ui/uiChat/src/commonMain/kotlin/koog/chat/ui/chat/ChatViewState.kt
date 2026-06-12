package koog.chat.ui.chat

import koog.chat.core.database.api.entity.LlmConfig

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
        val availableConfigs: List<LlmConfig> = emptyList(),
        val isModelPickerVisible: Boolean = false,
    ) : ChatViewState

    data class Error(
        val error: Throwable,
    ) : ChatViewState
}

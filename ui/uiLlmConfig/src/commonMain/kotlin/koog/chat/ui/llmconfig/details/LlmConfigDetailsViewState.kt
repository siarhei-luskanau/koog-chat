package koog.chat.ui.llmconfig.details

import koog.chat.core.database.api.entity.LlmProvider

data class LlmConfigDetailsViewState(
    val provider: LlmProvider = LlmProvider.Ollama,
    val modelId: String = "",
    val apiKey: String = "",
    val providerUrl: String = "",
    val isDefault: Boolean = false,
    val isNew: Boolean = true,
    val canDelete: Boolean = false,
) {
    val canSave: Boolean
        get() = modelId.isNotBlank()
}

package koog.chat.ui.llmconfig.list

import koog.chat.core.database.api.entity.LlmConfig

sealed interface LlmConfigListViewState {
    data object Loading : LlmConfigListViewState

    data class Success(
        val configs: List<LlmConfig>,
    ) : LlmConfigListViewState
}

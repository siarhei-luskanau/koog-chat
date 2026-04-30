package koog.chat.ui.llmconfig.details

import koog.chat.core.database.api.entity.LlmProvider

sealed interface LlmConfigDetailsViewEvent {
    data class ProviderChanged(
        val provider: LlmProvider,
    ) : LlmConfigDetailsViewEvent

    data class ModelIdChanged(
        val value: String,
    ) : LlmConfigDetailsViewEvent

    data class ApiKeyChanged(
        val value: String,
    ) : LlmConfigDetailsViewEvent

    data class ProviderUrlChanged(
        val value: String,
    ) : LlmConfigDetailsViewEvent

    data object ToggleDefault : LlmConfigDetailsViewEvent

    data object Save : LlmConfigDetailsViewEvent

    data object Delete : LlmConfigDetailsViewEvent

    data object NavigateBack : LlmConfigDetailsViewEvent
}

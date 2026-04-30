package koog.chat.ui.llmconfig.list

sealed interface LlmConfigListViewEvent {
    data class OpenDetails(
        val id: String,
    ) : LlmConfigListViewEvent

    data object AddNew : LlmConfigListViewEvent

    data class Delete(
        val id: String,
    ) : LlmConfigListViewEvent

    data object NavigateBack : LlmConfigListViewEvent
}

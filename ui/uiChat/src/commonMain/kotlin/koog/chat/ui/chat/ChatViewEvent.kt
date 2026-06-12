package koog.chat.ui.chat

sealed interface ChatViewEvent {
    data object NavigateBack : ChatViewEvent

    data class InputChanged(
        val text: String,
    ) : ChatViewEvent

    data object SendMessage : ChatViewEvent

    data object PickModel : ChatViewEvent

    data class SelectModel(
        val configId: String,
    ) : ChatViewEvent
}

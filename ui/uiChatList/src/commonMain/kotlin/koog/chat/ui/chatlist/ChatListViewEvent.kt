package koog.chat.ui.chatlist

sealed interface ChatListViewEvent {
    data class OpenChat(
        val chatId: String,
    ) : ChatListViewEvent

    data object NewChat : ChatListViewEvent

    data object ToggleSearch : ChatListViewEvent

    data class SearchQueryChanged(
        val query: String,
    ) : ChatListViewEvent
}

package koog.chat.ui.chatlist

import koog.chat.core.pref.AppMode

sealed interface ChatListViewEvent {
    data class OpenChat(
        val chatId: String,
    ) : ChatListViewEvent

    data object NewChat : ChatListViewEvent

    data object ToggleSearch : ChatListViewEvent

    data class SearchQueryChanged(
        val query: String,
    ) : ChatListViewEvent

    data object ToggleSettings : ChatListViewEvent

    data class SetAppMode(
        val mode: AppMode,
    ) : ChatListViewEvent
}

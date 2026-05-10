package koog.chat.ui.chatlist

import androidx.compose.runtime.Immutable

@Immutable
data class ChatListEntry(
    val id: String,
    val title: String,
    val timestamp: String,
    val preview: String = "",
    val modelName: String? = null,
    val messageCount: Int? = null,
    val avatarColorIndex: Int = 0,
)

data class ChatDateGroup(
    val label: String,
    val items: List<ChatListEntry>,
)

sealed interface ChatListViewState {
    data object Loading : ChatListViewState

    data class Success(
        val groups: List<ChatDateGroup>,
        val searchQuery: String = "",
        val isSearchVisible: Boolean = false,
    ) : ChatListViewState

    data class Error(
        val error: Throwable,
    ) : ChatListViewState
}

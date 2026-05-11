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

sealed interface ChatPagingItem {
    data class Header(
        val label: String,
    ) : ChatPagingItem

    data class Entry(
        val chat: ChatListEntry,
        val createdAt: kotlin.time.Instant,
    ) : ChatPagingItem
}

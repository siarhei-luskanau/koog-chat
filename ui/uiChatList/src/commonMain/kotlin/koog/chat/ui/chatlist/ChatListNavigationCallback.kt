package koog.chat.ui.chatlist

interface ChatListNavigationCallback {
    fun openChat(chatId: String)

    fun openNewChat()

    fun openLlmConfigList()
}

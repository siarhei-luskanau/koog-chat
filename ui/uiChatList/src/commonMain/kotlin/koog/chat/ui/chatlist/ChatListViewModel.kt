package koog.chat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.common.DispatcherSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ChatListViewModel(
    @InjectedParam private val initArg: String,
    @Provided private val navigationCallback: ChatListNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
) : ViewModel() {
    val viewState: StateFlow<ChatListViewState>
        field = MutableStateFlow<ChatListViewState>(ChatListViewState.Loading)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            viewState.value = ChatListViewState.Success(groups = sampleGroups())
        }
    }

    fun onEvent(event: ChatListViewEvent) {
        viewModelScope.launch {
            when (event) {
                is ChatListViewEvent.OpenChat -> navigationCallback.openChat(event.chatId)
                ChatListViewEvent.NewChat -> navigationCallback.openNewChat()
                ChatListViewEvent.ToggleSearch -> toggleSearch()
                is ChatListViewEvent.SearchQueryChanged -> updateSearch(event.query)
            }
        }
    }

    private fun toggleSearch() {
        val current = viewState.value
        if (current is ChatListViewState.Success) {
            viewState.value = current.copy(isSearchVisible = !current.isSearchVisible)
        }
    }

    private fun updateSearch(query: String) {
        val current = viewState.value
        if (current is ChatListViewState.Success) {
            viewState.value = current.copy(searchQuery = query)
        }
    }

    private fun sampleGroups() =
        listOf(
            ChatDateGroup(
                label = "Today",
                items =
                    listOf(
                        ChatListEntry(
                            id = "1",
                            title = "Compose design system",
                            timestamp = "14:32",
                            preview = "How do I set up Material3 with custom tokens in Compose Multiplatform?",
                            modelName = "claude-3-5-sonnet",
                            messageCount = 12,
                            avatarColorIndex = 1,
                        ),
                        ChatListEntry(
                            id = "2",
                            title = "Kotlin coroutines",
                            timestamp = "11:05",
                            preview = "Explain the difference between launch and async in Kotlin coroutines.",
                            modelName = "claude-3-haiku",
                            messageCount = 8,
                            avatarColorIndex = 0,
                        ),
                    ),
            ),
            ChatDateGroup(
                label = "Yesterday",
                items =
                    listOf(
                        ChatListEntry(
                            id = "3",
                            title = "KMP build setup",
                            timestamp = "Yesterday",
                            preview = "How to configure Gradle for Kotlin Multiplatform with iOS and Android targets?",
                            modelName = "claude-3-5-haiku",
                            messageCount = 5,
                            avatarColorIndex = 2,
                        ),
                    ),
            ),
            ChatDateGroup(
                label = "This week",
                items =
                    listOf(
                        ChatListEntry(
                            id = "4",
                            title = "SQL optimization tips",
                            timestamp = "Mon",
                            preview = "What are the best practices for optimizing slow SQL queries on large tables?",
                            avatarColorIndex = 0,
                        ),
                        ChatListEntry(
                            id = "5",
                            title = "SwiftUI vs Compose",
                            timestamp = "Sun",
                            preview = "Compare SwiftUI and Compose Multiplatform for cross-platform development.",
                            modelName = "claude-3-opus",
                            messageCount = 22,
                            avatarColorIndex = 1,
                        ),
                    ),
            ),
        )
}

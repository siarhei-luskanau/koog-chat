package koog.chat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.time.Clock
import kotlin.time.Instant

@KoinViewModel
class ChatListViewModel(
    @Provided private val navigationCallback: ChatListNavigationCallback,
    @Provided private val chatRepository: ChatRepository,
) : ViewModel() {
    val isSearchVisible: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val searchQuery: StateFlow<String>
        field = MutableStateFlow("")

    val pagingDataFlow: Flow<PagingData<ChatPagingItem>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            chatRepository.pagingSource()
        }.flow
            .map { pagingData ->
                pagingData
                    .map { chat ->
                        ChatPagingItem.Entry(
                            chat = chat.toChatListEntry(),
                            createdAt = Instant.fromEpochMilliseconds(chat.createdAt),
                        )
                    }.insertSeparators { before, after ->
                        if (after == null) return@insertSeparators null
                        val afterLabel = dateGroupLabel(after.createdAt)
                        if (before == null || dateGroupLabel(before.createdAt) != afterLabel) {
                            ChatPagingItem.Header(afterLabel)
                        } else {
                            null
                        }
                    }
            }.cachedIn(viewModelScope)

    fun onEvent(event: ChatListViewEvent) {
        viewModelScope.launch {
            when (event) {
                is ChatListViewEvent.OpenChat -> navigationCallback.openChat(event.chatId)
                ChatListViewEvent.NewChat -> navigationCallback.openNewChat()
                ChatListViewEvent.ToggleSearch -> isSearchVisible.value = !isSearchVisible.value
                is ChatListViewEvent.SearchQueryChanged -> searchQuery.value = event.query
            }
        }
    }

    private fun dateGroupLabel(createdAt: Instant): String {
        val tz = TimeZone.currentSystemDefault()
        val today =
            Clock.System
                .now()
                .toLocalDateTime(tz)
                .date
        val chatDate = createdAt.toLocalDateTime(tz).date
        val daysDiff = (today.toEpochDays() - chatDate.toEpochDays()).toInt()
        return when {
            daysDiff == 0 -> "Today"
            daysDiff == 1 -> "Yesterday"
            daysDiff < 7 -> "This week"
            else -> "Older"
        }
    }

    private fun Chat.toChatListEntry(): ChatListEntry {
        val tz = TimeZone.currentSystemDefault()
        val localDateTime = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(tz)
        val today =
            Clock.System
                .now()
                .toLocalDateTime(tz)
                .date
        val chatDate = localDateTime.date
        val daysDiff = (today.toEpochDays() - chatDate.toEpochDays()).toInt()
        val timestamp =
            when {
                daysDiff == 0 -> {
                    "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                }

                daysDiff == 1 -> {
                    "Yesterday"
                }

                daysDiff < 7 -> {
                    localDateTime.dayOfWeek.name
                        .take(3)
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                }

                else -> {
                    "${chatDate.year}-${chatDate.monthNumber.toString().padStart(
                        2,
                        '0',
                    )}-${chatDate.dayOfMonth.toString().padStart(2, '0')}"
                }
            }
        return ChatListEntry(id = id, title = title, timestamp = timestamp)
    }
}

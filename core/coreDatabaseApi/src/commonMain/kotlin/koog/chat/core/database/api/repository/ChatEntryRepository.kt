package koog.chat.core.database.api.repository

import androidx.paging.PagingSource
import koog.chat.core.database.api.entity.ChatEntry

interface ChatEntryRepository {
    fun pagingSource(chatId: String): PagingSource<Int, ChatEntry>

    suspend fun save(entry: ChatEntry)

    suspend fun update(entry: ChatEntry)
}

package koog.chat.core.database.api.repository

import androidx.paging.PagingSource
import koog.chat.core.database.api.entity.Chat

interface ChatRepository {
    fun pagingSource(): PagingSource<Int, Chat>

    suspend fun getById(id: String): Chat?

    suspend fun save(chat: Chat)

    suspend fun delete(id: String)
}

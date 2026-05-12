package koog.chat.core.database.room.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import koog.chat.core.database.room.entity.ChatEntryEntity

@Dao
interface ChatEntryDao {
    @Query("SELECT * FROM chat_entries WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun pagingSource(chatId: String): PagingSource<Int, ChatEntryEntity>

    @Upsert
    suspend fun upsert(entity: ChatEntryEntity)
}

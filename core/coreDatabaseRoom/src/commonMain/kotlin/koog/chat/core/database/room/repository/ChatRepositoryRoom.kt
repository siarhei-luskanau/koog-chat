package koog.chat.core.database.room.repository

import androidx.paging.PagingSource
import koog.chat.core.database.api.entity.Chat
import koog.chat.core.database.api.repository.ChatRepository
import koog.chat.core.database.room.RoomDatabaseProvider
import koog.chat.core.database.room.entity.ChatEntity
import koog.chat.core.database.room.util.map
import org.koin.core.annotation.Single

@Single
internal class ChatRepositoryRoom(
    private val provider: RoomDatabaseProvider,
) : ChatRepository {
    override fun pagingSource(): PagingSource<Int, Chat> =
        provider.database
            .chatDao()
            .pagingSource()
            .map { it.toDomain() }

    override suspend fun getById(id: String): Chat? =
        provider.database
            .chatDao()
            .getById(id)
            ?.toDomain()

    override suspend fun save(chat: Chat) = provider.database.chatDao().upsert(chat.toEntity())

    override suspend fun delete(id: String) = provider.database.chatDao().deleteById(id)

    private fun ChatEntity.toDomain() = Chat(id = id, title = title, createdAt = createdAt)

    private fun Chat.toEntity() = ChatEntity(id = id, title = title, createdAt = createdAt)
}

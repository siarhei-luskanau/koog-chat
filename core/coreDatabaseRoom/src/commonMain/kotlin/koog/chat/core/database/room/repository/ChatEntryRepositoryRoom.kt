package koog.chat.core.database.room.repository

import androidx.paging.PagingSource
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.database.room.RoomDatabaseProvider
import koog.chat.core.database.room.entity.ChatEntryEntity
import koog.chat.core.database.room.util.map
import org.koin.core.annotation.Single

@Single
internal class ChatEntryRepositoryRoom(
    private val provider: RoomDatabaseProvider,
) : ChatEntryRepository {
    override fun pagingSource(chatId: String): PagingSource<Int, ChatEntry> =
        provider.database
            .chatEntryDao()
            .pagingSource(chatId)
            .map { it.toDomain() }

    override suspend fun save(entry: ChatEntry) = provider.database.chatEntryDao().upsert(entry.toEntity())

    override suspend fun update(entry: ChatEntry) = provider.database.chatEntryDao().upsert(entry.toEntity())

    private fun ChatEntryEntity.toDomain() =
        ChatEntry(
            id = id,
            chatId = chatId,
            type = ChatEntryType.valueOf(type),
            content = content,
            thinkingContent = thinkingContent,
            llmConfigId = llmConfigId,
            llmProvider = llmProvider,
            llmModelId = llmModelId,
            tokensUsed = tokensUsed,
            tokensPerSecond = tokensPerSecond,
            responseTimeMs = responseTimeMs,
            timestamp = timestamp,
        )

    private fun ChatEntry.toEntity() =
        ChatEntryEntity(
            id = id,
            chatId = chatId,
            type = type.name,
            content = content,
            thinkingContent = thinkingContent,
            llmConfigId = llmConfigId,
            llmProvider = llmProvider,
            llmModelId = llmModelId,
            tokensUsed = tokensUsed,
            tokensPerSecond = tokensPerSecond,
            responseTimeMs = responseTimeMs,
            timestamp = timestamp,
        )
}

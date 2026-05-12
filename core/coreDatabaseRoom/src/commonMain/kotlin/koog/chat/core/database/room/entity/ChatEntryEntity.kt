package koog.chat.core.database.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "chat_entries",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LlmConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["llmConfigId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("chatId"), Index("llmConfigId")],
)
data class ChatEntryEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val type: String,
    val content: String,
    val thinkingContent: String?,
    val llmConfigId: String?,
    val llmProvider: String,
    val llmModelId: String,
    val tokensUsed: Long?,
    val tokensPerSecond: Double?,
    val responseTimeMs: Long?,
    val timestamp: Long,
)

package koog.chat.core.database.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
)

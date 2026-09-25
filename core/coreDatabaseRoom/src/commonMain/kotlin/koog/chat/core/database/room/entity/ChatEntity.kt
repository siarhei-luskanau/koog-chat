package koog.chat.core.database.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0L,
    @ColumnInfo(defaultValue = "0") val isDirty: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false,
)

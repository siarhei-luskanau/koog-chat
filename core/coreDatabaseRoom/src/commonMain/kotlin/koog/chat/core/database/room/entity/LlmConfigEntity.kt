package koog.chat.core.database.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "llm_configs")
data class LlmConfigEntity(
    @PrimaryKey val id: String,
    val provider: String,
    val modelId: String,
    val apiKey: String?,
    val providerUrl: String?,
    val isDefault: Boolean,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0L,
    @ColumnInfo(defaultValue = "0") val isDirty: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false,
)

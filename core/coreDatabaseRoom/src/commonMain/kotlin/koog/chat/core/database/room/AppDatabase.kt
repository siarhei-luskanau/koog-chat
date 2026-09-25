package koog.chat.core.database.room

import androidx.room3.ConstructedBy
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import koog.chat.core.database.room.dao.ChatDao
import koog.chat.core.database.room.dao.ChatEntryDao
import koog.chat.core.database.room.dao.LlmConfigDao
import koog.chat.core.database.room.entity.ChatEntity
import koog.chat.core.database.room.entity.ChatEntryEntity
import koog.chat.core.database.room.entity.LlmConfigEntity

@Database(
    entities = [
        ChatEntity::class,
        ChatEntryEntity::class,
        LlmConfigEntity::class,
    ],
    version = 1,
)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    abstract fun chatEntryDao(): ChatEntryDao

    abstract fun llmConfigDao(): LlmConfigDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

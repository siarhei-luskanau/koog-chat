package koog.chat.core.database.room.repository

import koog.chat.core.database.DatabaseRecord
import koog.chat.core.database.DatabaseRepository
import koog.chat.core.database.room.RoomDatabaseProvider
import koog.chat.core.database.room.entity.ExampleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
internal class DatabaseRepositoryRoom(
    private val provider: RoomDatabaseProvider,
) : DatabaseRepository {
    override fun getAll(): Flow<List<DatabaseRecord>> =
        provider.database
            .exampleDao()
            .getAll()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun save(record: DatabaseRecord) = provider.database.exampleDao().upsert(record.toEntity())

    override suspend fun delete(id: String) = provider.database.exampleDao().deleteById(id)

    private fun ExampleEntity.toDomain() = DatabaseRecord(id = id, tag = tag)

    private fun DatabaseRecord.toEntity() = ExampleEntity(id = id, tag = tag)
}

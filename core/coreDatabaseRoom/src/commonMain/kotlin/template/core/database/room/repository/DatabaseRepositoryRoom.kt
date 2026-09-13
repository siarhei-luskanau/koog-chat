package template.core.database.room.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import template.core.database.DatabaseRecord
import template.core.database.DatabaseRepository
import template.core.database.room.RoomDatabaseProvider
import template.core.database.room.entity.ExampleEntity

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

package template.core.database

import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getAll(): Flow<List<DatabaseRecord>>

    suspend fun save(record: DatabaseRecord)

    suspend fun delete(id: String)
}

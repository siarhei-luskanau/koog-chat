package template.core.database.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import template.core.database.room.entity.ExampleEntity

@Dao
interface ExampleDao {
    @Query("SELECT * FROM examples")
    fun getAll(): Flow<List<ExampleEntity>>

    @Upsert
    suspend fun upsert(entity: ExampleEntity)

    @Query("DELETE FROM examples WHERE id = :id")
    suspend fun deleteById(id: String)
}

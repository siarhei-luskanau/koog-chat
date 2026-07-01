package koog.chat.core.database.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import koog.chat.core.database.room.entity.LlmConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LlmConfigDao {
    @Query("SELECT * FROM llm_configs ORDER BY isDefault DESC, modelId ASC")
    fun getAllFlow(): Flow<List<LlmConfigEntity>>

    @Query("SELECT * FROM llm_configs WHERE id = :id")
    suspend fun getById(id: String): LlmConfigEntity?

    @Upsert
    suspend fun upsert(entity: LlmConfigEntity)

    @Query("DELETE FROM llm_configs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE llm_configs SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE llm_configs SET isDefault = 1 WHERE id = :id")
    suspend fun markDefault(id: String)

    @Transaction
    suspend fun setDefault(id: String) {
        clearDefault()
        markDefault(id)
    }
}

package koog.chat.core.database.api.repository

import koog.chat.core.database.api.entity.LlmConfig
import kotlinx.coroutines.flow.Flow

interface LlmConfigRepository {
    fun getAllFlow(): Flow<List<LlmConfig>>

    suspend fun getById(id: String): LlmConfig?

    suspend fun save(config: LlmConfig)

    suspend fun delete(id: String)

    suspend fun setDefault(id: String)
}

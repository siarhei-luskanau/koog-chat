package koog.chat.core.database.room.repository

import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.core.database.room.RoomDatabaseProvider
import koog.chat.core.database.room.entity.LlmConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single
internal class LlmConfigRepositoryRoom(
    private val provider: RoomDatabaseProvider,
) : LlmConfigRepository {
    override fun getAllFlow(): Flow<List<LlmConfig>> =
        provider.database
            .llmConfigDao()
            .getAllFlow()
            .onEach { list ->
                if (list.isEmpty()) {
                    save(
                        LlmConfig(
                            id = "default-ollama-qwen",
                            provider = LlmProvider.Ollama,
                            modelId = "qwen3.5:0.8b",
                            apiKey = null,
                            providerUrl = null,
                            isDefault = true,
                        ),
                    )
                }
            }.map { list -> list.map { it.toDomain() } }

    override suspend fun save(config: LlmConfig) = provider.database.llmConfigDao().upsert(config.toEntity())

    override suspend fun delete(id: String) = provider.database.llmConfigDao().deleteById(id)

    private fun LlmConfigEntity.toDomain() =
        LlmConfig(
            id = id,
            provider = LlmProvider.valueOf(provider),
            modelId = modelId,
            apiKey = apiKey,
            providerUrl = providerUrl,
            isDefault = isDefault,
        )

    private fun LlmConfig.toEntity() =
        LlmConfigEntity(
            id = id,
            provider = provider.name,
            modelId = modelId,
            apiKey = apiKey,
            providerUrl = providerUrl,
            isDefault = isDefault,
        )
}

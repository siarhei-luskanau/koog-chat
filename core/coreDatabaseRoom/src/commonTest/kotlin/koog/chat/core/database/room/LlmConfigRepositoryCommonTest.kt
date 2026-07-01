package koog.chat.core.database.room

import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.LlmConfigRepository
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LlmConfigRepositoryCommonTest {
    @Test
    fun saveAndGetById() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config =
                LlmConfig(
                    id = "test-1",
                    provider = LlmProvider.Ollama,
                    modelId = "qwen3.5:0.8b",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = false,
                )
            repo.save(config)
            assertEquals(config, repo.getById("test-1"))
            koinApplication.close()
        }

    @Test
    fun deleteById() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config =
                LlmConfig(
                    id = "test-2",
                    provider = LlmProvider.Ollama,
                    modelId = "qwen3.5:0.8b",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = false,
                )
            repo.save(config)
            repo.delete("test-2")
            assertNull(repo.getById("test-2"))
            koinApplication.close()
        }

    @Test
    fun setDefaultClearsPreviousDefault() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val configA =
                LlmConfig(
                    id = "test-a",
                    provider = LlmProvider.Ollama,
                    modelId = "model-a",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = true,
                )
            val configB =
                LlmConfig(
                    id = "test-b",
                    provider = LlmProvider.Ollama,
                    modelId = "model-b",
                    apiKey = null,
                    providerUrl = null,
                    isDefault = false,
                )
            repo.save(configA)
            repo.save(configB)

            repo.setDefault("test-b")

            assertEquals(false, repo.getById("test-a")?.isDefault)
            assertEquals(true, repo.getById("test-b")?.isDefault)
            koinApplication.close()
        }
}

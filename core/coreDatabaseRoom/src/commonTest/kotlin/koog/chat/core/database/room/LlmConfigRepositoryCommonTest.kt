package koog.chat.core.database.room

import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.core.database.api.repository.LlmConfigRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LlmConfigRepositoryCommonTest {
    private fun uniqueId(prefix: String) = "$prefix-${Random.nextLong()}"

    private fun testConfig(
        id: String = uniqueId("config"),
        provider: LlmProvider = LlmProvider.Ollama,
        modelId: String = "qwen3.5:0.8b",
        apiKey: String? = null,
        providerUrl: String? = null,
        isDefault: Boolean = false,
    ) = LlmConfig(
        id = id,
        provider = provider,
        modelId = modelId,
        apiKey = apiKey,
        providerUrl = providerUrl,
        isDefault = isDefault,
    )

    @Test
    fun saveAndGetById_shouldReturnSavedConfig() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config = testConfig()
            repo.save(config)
            assertEquals(config, repo.getById(config.id))
            repo.delete(config.id)
            koinApplication.close()
        }

    @Test
    fun deleteById_shouldRemoveConfig() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config = testConfig()
            repo.save(config)
            repo.delete(config.id)
            assertNull(repo.getById(config.id))
            koinApplication.close()
        }

    @Test
    fun setDefault_shouldClearPreviousDefault_whenSameProvider() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val configA = testConfig(isDefault = true)
            val configB = testConfig()
            repo.save(configA)
            repo.save(configB)

            repo.setDefault(configB.id)

            assertEquals(false, repo.getById(configA.id)?.isDefault)
            assertEquals(true, repo.getById(configB.id)?.isDefault)

            repo.delete(configA.id)
            repo.delete(configB.id)
            koinApplication.close()
        }

    @Test
    fun setDefault_shouldOnlyClearDefaultsOfSameProvider() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val ollamaDefault = testConfig(provider = LlmProvider.Ollama, isDefault = true)
            val ollamaOther = testConfig(provider = LlmProvider.Ollama)
            val openAiDefault = testConfig(provider = LlmProvider.OpenAI, isDefault = true)
            repo.save(ollamaDefault)
            repo.save(ollamaOther)
            repo.save(openAiDefault)

            repo.setDefault(ollamaOther.id)

            assertFalse(repo.getById(ollamaDefault.id)!!.isDefault)
            assertTrue(repo.getById(ollamaOther.id)!!.isDefault)
            assertTrue(repo.getById(openAiDefault.id)!!.isDefault)

            repo.delete(ollamaDefault.id)
            repo.delete(ollamaOther.id)
            repo.delete(openAiDefault.id)
            koinApplication.close()
        }

    @Test
    fun save_shouldClearOtherDefaultsOfSameProviderOnly_whenSavedAsDefault() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val ollamaDefault = testConfig(provider = LlmProvider.Ollama, isDefault = true)
            val openAiDefault = testConfig(provider = LlmProvider.OpenAI, isDefault = true)
            repo.save(ollamaDefault)
            repo.save(openAiDefault)

            val newOllamaDefault = testConfig(provider = LlmProvider.Ollama, isDefault = true)
            repo.save(newOllamaDefault)

            assertFalse(repo.getById(ollamaDefault.id)!!.isDefault)
            assertTrue(repo.getById(newOllamaDefault.id)!!.isDefault)
            assertTrue(repo.getById(openAiDefault.id)!!.isDefault)

            repo.delete(ollamaDefault.id)
            repo.delete(openAiDefault.id)
            repo.delete(newOllamaDefault.id)
            koinApplication.close()
        }

    @Test
    fun setDefault_shouldLeaveConfigsUnchanged_whenIdDoesNotExist() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config = testConfig(isDefault = true)
            repo.save(config)

            repo.setDefault(uniqueId("missing"))

            assertEquals(true, repo.getById(config.id)?.isDefault)

            repo.delete(config.id)
            koinApplication.close()
        }

    @Test
    fun getAllFlow_shouldRoundTripEveryProvider() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val configs =
                LlmProvider.entries.map { provider ->
                    testConfig(provider = provider, apiKey = "key-$provider", providerUrl = "https://$provider.example")
                }
            configs.forEach { repo.save(it) }

            val ownIds = configs.map { it.id }.toSet()
            val ownConfigs = repo.getAllFlow().first().filter { it.id in ownIds }

            assertEquals(configs.toSet(), ownConfigs.toSet())
            assertEquals(LlmProvider.entries.toSet(), ownConfigs.map { it.provider }.toSet())

            configs.forEach { repo.delete(it.id) }
            koinApplication.close()
        }

    @Test
    fun getAllFlow_shouldNotSeedAnyConfig() =
        runTest {
            val koinApplication = koinApplication<TestKoinApplication>()
            val repo = koinApplication.koin.get<LlmConfigRepository>()
            val config = testConfig()
            repo.save(config)
            repo.delete(config.id)

            val all = repo.getAllFlow().first()

            assertNull(all.find { it.id == config.id })
            assertNull(all.find { it.id == "default-ollama-qwen" })
            koinApplication.close()
        }
}

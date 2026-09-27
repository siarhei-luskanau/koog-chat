package koog.chat.core.llm.koog

import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import io.ktor.client.engine.mock.respondOk
import koog.chat.core.database.api.entity.LlmProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class LlmClientFactoryCommonTest {
    private val factory = LlmClientFactory(RecordingMockEngine { respondOk() }.httpClientFactory)

    @Test
    fun createModelMapsEveryProviderToMatchingKoogProvider() {
        val expected =
            mapOf(
                LlmProvider.Ollama to LLMProvider.Ollama,
                LlmProvider.OpenAI to LLMProvider.OpenAI,
                LlmProvider.Anthropic to LLMProvider.Anthropic,
                LlmProvider.Google to LLMProvider.Google,
            )
        LlmProvider.entries.forEach { provider ->
            val model = factory.createModel(llmConfig(provider))
            assertEquals(expected.getValue(provider), model.provider)
            assertEquals("model-${provider.name.lowercase()}", model.id)
            assertTrue(model.supports(LLMCapability.Completion), "$provider must support completion")
        }
    }

    @Test
    fun openAiModelUsesChatCompletionsEndpoint() {
        val model = factory.createModel(llmConfig(LlmProvider.OpenAI))
        assertTrue(model.supports(LLMCapability.OpenAIEndpoint.Completions))
    }

    @Test
    fun createClientDispatchesPerProvider() {
        LlmProvider.entries.forEach { provider ->
            val config = llmConfig(provider)
            val client = factory.createClient(config, factory.createModel(config))
            when (provider) {
                LlmProvider.Ollama -> assertIs<OllamaClient>(client)
                LlmProvider.OpenAI -> assertIs<OpenAILLMClient>(client)
                LlmProvider.Anthropic -> assertIs<AnthropicLLMClient>(client)
                LlmProvider.Google -> assertIs<GoogleLLMClient>(client)
            }
            client.close()
        }
    }

    @Test
    fun ollamaDoesNotRequireApiKey() {
        val config = llmConfig(LlmProvider.Ollama, apiKey = null)
        factory.createClient(config, factory.createModel(config)).close()
    }

    @Test
    fun cloudProvidersRequireApiKey() {
        listOf(LlmProvider.OpenAI, LlmProvider.Anthropic, LlmProvider.Google).forEach { provider ->
            listOf(null, " ").forEach { apiKey ->
                val config = llmConfig(provider, apiKey = apiKey)
                val error =
                    assertFailsWith<IllegalArgumentException> {
                        factory.createClient(config, factory.createModel(config))
                    }
                assertEquals("API key is required for $provider", error.message)
            }
        }
    }
}

package koog.chat.core.llm.koog

import ai.koog.http.client.KoogHttpClient
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import org.koin.core.annotation.Single

@Single
internal class LlmClientFactory(
    private val httpClientFactory: KoogHttpClient.Factory,
) {
    fun createModel(config: LlmConfig): LLModel =
        when (config.provider) {
            LlmProvider.Ollama -> {
                LLModel(
                    provider = LLMProvider.Ollama,
                    id = config.modelId,
                    capabilities = listOf(LLMCapability.Completion, LLMCapability.Thinking),
                    contextLength = DEFAULT_CONTEXT_LENGTH,
                )
            }

            LlmProvider.OpenAI -> {
                LLModel(
                    provider = LLMProvider.OpenAI,
                    id = config.modelId,
                    capabilities = listOf(LLMCapability.Completion, LLMCapability.OpenAIEndpoint.Completions),
                    contextLength = DEFAULT_CONTEXT_LENGTH,
                )
            }

            LlmProvider.Anthropic -> {
                LLModel(
                    provider = LLMProvider.Anthropic,
                    id = config.modelId,
                    capabilities = listOf(LLMCapability.Completion),
                    contextLength = DEFAULT_CONTEXT_LENGTH,
                )
            }

            LlmProvider.Google -> {
                LLModel(
                    provider = LLMProvider.Google,
                    id = config.modelId,
                    capabilities = listOf(LLMCapability.Completion),
                    contextLength = DEFAULT_CONTEXT_LENGTH,
                )
            }
        }

    fun createClient(
        config: LlmConfig,
        model: LLModel,
    ): LLMClient {
        val providerUrl = config.providerUrl?.takeIf { it.isNotBlank() }
        return when (config.provider) {
            LlmProvider.Ollama -> {
                OllamaClient(
                    httpClientFactory = httpClientFactory,
                    baseUrl = providerUrl ?: DEFAULT_OLLAMA_URL,
                )
            }

            LlmProvider.OpenAI -> {
                OpenAILLMClient(
                    apiKey = config.requireApiKey(),
                    settings = providerUrl?.let { OpenAIClientSettings(baseUrl = it) } ?: OpenAIClientSettings(),
                    httpClientFactory = httpClientFactory,
                )
            }

            LlmProvider.Anthropic -> {
                val modelVersionsMap = mapOf(model to model.id)
                AnthropicLLMClient(
                    apiKey = config.requireApiKey(),
                    settings =
                        providerUrl?.let { AnthropicClientSettings(modelVersionsMap = modelVersionsMap, baseUrl = it) }
                            ?: AnthropicClientSettings(modelVersionsMap = modelVersionsMap),
                    httpClientFactory = httpClientFactory,
                )
            }

            LlmProvider.Google -> {
                GoogleLLMClient(
                    apiKey = config.requireApiKey(),
                    settings = providerUrl?.let { GoogleClientSettings(baseUrl = it) } ?: GoogleClientSettings(),
                    httpClientFactory = httpClientFactory,
                )
            }
        }
    }

    private fun LlmConfig.requireApiKey(): String =
        requireNotNull(apiKey?.takeIf { it.isNotBlank() }) { "API key is required for $provider" }

    private companion object {
        const val DEFAULT_OLLAMA_URL = "http://localhost:11434"
        const val DEFAULT_CONTEXT_LENGTH = 256_000L
    }
}

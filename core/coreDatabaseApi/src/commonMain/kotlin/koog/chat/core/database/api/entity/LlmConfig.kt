package koog.chat.core.database.api.entity

data class LlmConfig(
    val id: String,
    val provider: LlmProvider,
    val modelId: String,
    val apiKey: String?,
    val providerUrl: String?,
    val isDefault: Boolean,
)

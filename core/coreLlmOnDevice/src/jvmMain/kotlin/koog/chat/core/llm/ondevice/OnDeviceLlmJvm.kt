package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

private const val UNAVAILABLE_REASON = "No on-device model on desktop JVM"

@Single
internal class OnDeviceLlmJvm : OnDeviceLlm {
    override val availability: StateFlow<OnDeviceAvailability> =
        MutableStateFlow(OnDeviceAvailability.Unavailable(UNAVAILABLE_REASON))

    override val maxContextTokens: Int = DEFAULT_MAX_CONTEXT_TOKENS

    override suspend fun refreshAvailability() = Unit

    override suspend fun download() = Unit

    override fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk> = flow { throw IllegalStateException(UNAVAILABLE_REASON) }
}

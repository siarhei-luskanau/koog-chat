package koog.chat.core.llm.ondevice

import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

private const val BRIDGE_NOT_REGISTERED = "Apple Foundation Models bridge is not registered"
private const val AVAILABLE = "available"

@Single
internal class OnDeviceLlmIos : OnDeviceLlm {
    private val mutableAvailability = MutableStateFlow(resolveAvailability())

    override val availability: StateFlow<OnDeviceAvailability> = mutableAvailability

    override val maxContextTokens: Int = DEFAULT_MAX_CONTEXT_TOKENS

    override suspend fun download() = Unit

    override suspend fun refreshAvailability() {
        mutableAvailability.value = resolveAvailability()
    }

    override fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk> {
        val bridge = AppleFoundationModelsBridgeRegistry.bridge
        return if (bridge == null) {
            flow { throw IllegalStateException(BRIDGE_NOT_REGISTERED) }
        } else {
            bridge.streamChunks(formatTranscript(history, prompt))
        }
    }

    private fun resolveAvailability(): OnDeviceAvailability {
        val bridge = AppleFoundationModelsBridgeRegistry.bridge ?: return OnDeviceAvailability.Unavailable(BRIDGE_NOT_REGISTERED)
        val status = bridge.availability()
        return if (status == AVAILABLE) OnDeviceAvailability.Available else OnDeviceAvailability.Unavailable(status)
    }

    private fun AppleFoundationModelsBridge.streamChunks(prompt: String): Flow<LlmChunk> =
        callbackFlow {
            val deltas = SnapshotDeltas()
            val handle =
                stream(
                    prompt,
                    object : AppleFoundationModelsListener {
                        override fun onSnapshot(text: String) {
                            try {
                                deltas.next(text)?.let { trySend(LlmChunk.Text(it)) }
                            } catch (e: IllegalStateException) {
                                close(e)
                            }
                        }

                        override fun onComplete(failure: String?) {
                            if (failure == null) {
                                trySend(LlmChunk.End(null))
                                close()
                            } else {
                                close(Exception(failure))
                            }
                        }
                    },
                )
            awaitClose { handle.cancel() }
        }.buffer(Channel.UNLIMITED)
}

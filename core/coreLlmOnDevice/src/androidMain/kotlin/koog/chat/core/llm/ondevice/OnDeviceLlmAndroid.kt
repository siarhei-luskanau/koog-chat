package koog.chat.core.llm.ondevice

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
internal class OnDeviceLlmAndroid(
    @Provided dispatcherSet: DispatcherSet,
    private val model: GeminiNanoModel,
) : OnDeviceLlm {
    private val scope = CoroutineScope(SupervisorJob() + dispatcherSet.defaultDispatcher())
    private val mutableAvailability =
        MutableStateFlow<OnDeviceAvailability>(OnDeviceAvailability.Unavailable(CHECKING_REASON))

    @Volatile
    private var tokenLimit: Int = DEFAULT_MAX_CONTEXT_TOKENS

    override val availability: StateFlow<OnDeviceAvailability> = mutableAvailability

    override val maxContextTokens: Int get() = tokenLimit

    init {
        scope.launch { refreshAvailability() }
    }

    override suspend fun download() {
        var failure: Throwable? = null
        try {
            collectDownload()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure = e
        } finally {
            refreshAvailability()
        }
        failure?.let { throw it }
    }

    override fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk> =
        flow {
            val request = formatTranscript(history, prompt)
            model.generate(request).collect { text ->
                if (text.isNotEmpty()) emit(LlmChunk.Text(text))
            }
            emit(LlmChunk.End(null))
        }

    private suspend fun collectDownload() {
        var bytesToDownload = 0L
        model.download().collect { status ->
            when (status) {
                is GeminiNanoDownload.Started -> {
                    bytesToDownload = status.bytesToDownload
                }

                is GeminiNanoDownload.Progress -> {
                    mutableAvailability.value =
                        OnDeviceAvailability.Downloading(downloadFraction(status.bytesDownloaded, bytesToDownload))
                }

                is GeminiNanoDownload.Failed -> {
                    throw status.cause
                }

                GeminiNanoDownload.Completed -> {
                    Unit
                }
            }
        }
    }

    override suspend fun refreshAvailability() {
        mutableAvailability.value =
            try {
                mapFeatureStatus(model.checkStatus()).also { if (it is OnDeviceAvailability.Available) readTokenLimit() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                OnDeviceAvailability.Unavailable(e.message ?: UNSUPPORTED_REASON)
            }
    }

    private suspend fun readTokenLimit() {
        try {
            tokenLimit = model.tokenLimit()
        } catch (e: CancellationException) {
            throw e
        } catch (ignored: Exception) {
            tokenLimit = DEFAULT_MAX_CONTEXT_TOKENS
        }
    }
}

internal fun downloadFraction(
    downloaded: Long,
    total: Long,
): Float? = if (total > 0) (downloaded.toFloat() / total).coerceIn(0f, 1f) else null

@file:OptIn(ExperimentalWasmJsInterop::class)

package koog.chat.core.llm.ondevice

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.llm.LlmChunk
import koog.chat.core.llm.OnDeviceAvailability
import koog.chat.core.llm.OnDeviceLlm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

private const val USER_ROLE = "user"
private const val ASSISTANT_ROLE = "assistant"

@Single
internal class OnDeviceLlmWeb(
    @Provided dispatcherSet: DispatcherSet,
) : OnDeviceLlm {
    private val scope = CoroutineScope(SupervisorJob() + dispatcherSet.mainDispatcher())
    private val options: LanguageModelOptions = englishTextOptions()
    private val mutableAvailability =
        MutableStateFlow<OnDeviceAvailability>(OnDeviceAvailability.Unavailable(CHECKING_REASON))

    private var contextTokens: Int = DEFAULT_MAX_CONTEXT_TOKENS

    override val availability: StateFlow<OnDeviceAvailability> = mutableAvailability.asStateFlow()

    override val maxContextTokens: Int get() = contextTokens

    init {
        scope.launch { refreshAvailability() }
    }

    override suspend fun refreshAvailability() {
        mutableAvailability.value =
            try {
                if (isLanguageModelDefined()) {
                    chromeAvailability(jsValueText(queryAvailability(options).await()))
                } else {
                    chromeAvailability(null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                OnDeviceAvailability.Unavailable(jsErrorText(e))
            }
    }

    override suspend fun download() {
        if (mutableAvailability.value != OnDeviceAvailability.Downloadable) return
        try {
            val creation = createSessionWithDownloadMonitor(options) { loaded -> onDownloadProgress(loaded) }
            mutableAvailability.value = OnDeviceAvailability.Downloading(progress = null)
            val session = awaitSession(creation)
            rememberContextWindow(session)
            destroySession(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw ChromePromptApiException(jsErrorText(e), e)
        } finally {
            withContext(NonCancellable) { refreshAvailability() }
        }
    }

    override fun stream(
        history: List<ChatEntry>,
        prompt: String,
    ): Flow<LlmChunk> =
        flow {
            val handle = newAbortHandle()
            try {
                val session = awaitSession(createSession(options, initialPromptsOf(history), handle))
                try {
                    rememberContextWindow(session)
                    emitReply(session, prompt, handle)
                } finally {
                    destroySession(session)
                }
            } catch (e: CancellationException) {
                abortHandle(handle)
                throw e
            }
        }.catch { error ->
            if (error is CancellationException) throw error
            throw ChromePromptApiException(jsErrorText(error), error)
        }

    private suspend fun awaitSession(creation: Promise<LanguageModelSession>): LanguageModelSession =
        try {
            creation.await()
        } catch (e: CancellationException) {
            destroyWhenCreated(creation)
            throw e
        }

    private suspend fun FlowCollector<LlmChunk>.emitReply(
        session: LanguageModelSession,
        prompt: String,
        handle: AbortHandle,
    ) {
        val reader = startPromptStreaming(session, prompt, handle)
        var isFinished = false
        try {
            while (!isFinished) {
                val result = readNext(reader).await()
                isFinished = isReadDone(result)
                if (!isFinished) emitNonEmpty(readChunkText(result))
            }
        } finally {
            if (!isFinished) cancelReader(reader)
        }
        emit(LlmChunk.End(tokensUsed = null))
    }

    private suspend fun FlowCollector<LlmChunk>.emitNonEmpty(text: String) {
        if (text.isNotEmpty()) emit(LlmChunk.Text(text))
    }

    private fun onDownloadProgress(loaded: Double) {
        mutableAvailability.value = OnDeviceAvailability.Downloading(progress = loaded.toFloat())
    }

    private fun rememberContextWindow(session: LanguageModelSession) {
        val contextWindow = sessionContextWindow(session)
        if (contextWindow.isFinite() && contextWindow > 0) contextTokens = contextWindow.toInt()
    }
}

private fun initialPromptsOf(history: List<ChatEntry>): InitialPrompts {
    val prompts = emptyInitialPrompts()
    history.forEach { entry ->
        promptRoleOf(entry.type)?.let { role -> appendInitialPrompt(prompts, role, entry.content) }
    }
    return prompts
}

private fun promptRoleOf(type: ChatEntryType): String? =
    when (type) {
        ChatEntryType.USER_PROMPT -> USER_ROLE
        ChatEntryType.SUCCESS_RESPONSE -> ASSISTANT_ROLE
        else -> null
    }

package koog.chat.core.llm.koog

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.llm.ChatResult
import koog.chat.core.llm.LlmService
import koog.chat.core.llm.LlmSessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Single
internal class LlmSessionManagerImpl(
    @Provided private val chatEntryRepository: ChatEntryRepository,
    private val llmService: LlmService,
    @Provided dispatcherSet: DispatcherSet,
) : LlmSessionManager {
    private val sessionScope = CoroutineScope(SupervisorJob() + dispatcherSet.defaultDispatcher())
    private val activeChatsState = MutableStateFlow<Set<String>>(emptySet())

    override val activeChats: StateFlow<Set<String>> = activeChatsState.asStateFlow()

    override fun isGenerating(chatId: String): Flow<Boolean> = activeChatsState.map { it.contains(chatId) }

    override fun sendMessage(
        chatId: String,
        userText: String,
        config: LlmConfig,
    ) {
        activeChatsState.update { it + chatId }
        sessionScope.launch {
            executeChatTurn(chatId = chatId, userText = userText, config = config)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun executeChatTurn(
        chatId: String,
        userText: String,
        config: LlmConfig,
    ) {
        val userEntry = newEntry(chatId = chatId, type = ChatEntryType.USER_PROMPT, content = userText, config = config)
        val thinkingEntry = newEntry(chatId = chatId, type = ChatEntryType.THINKING, content = "", config = config)
        var placeholderSaved = false
        try {
            val priorHistory = chatEntryRepository.getAll(chatId)
            chatEntryRepository.save(userEntry)
            chatEntryRepository.save(thinkingEntry)
            placeholderSaved = true
            val streamingState = StreamingState(thinkingEntry = thinkingEntry, repository = chatEntryRepository)
            val result =
                llmService.chat(
                    messages = priorHistory + userEntry,
                    config = config,
                    onThinkingChunk = { chunk -> streamingState.appendThinking(chunk) },
                    onTextChunk = { chunk -> streamingState.appendText(chunk) },
                )
            chatEntryRepository.update(successEntryFor(placeholder = thinkingEntry, result = result))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            val errorEntry = errorEntryFor(placeholder = thinkingEntry, error = error, apiKey = config.apiKey)
            if (placeholderSaved) chatEntryRepository.update(errorEntry) else chatEntryRepository.save(errorEntry)
        } finally {
            activeChatsState.update { it - chatId }
        }
    }

    private fun newEntry(
        chatId: String,
        type: ChatEntryType,
        content: String,
        config: LlmConfig,
    ) = ChatEntry(
        id = Uuid.random().toString(),
        chatId = chatId,
        type = type,
        content = content,
        thinkingContent = if (type == ChatEntryType.THINKING) "" else null,
        llmConfigId = config.id,
        llmProvider = config.provider.name,
        llmModelId = config.modelId,
        tokensUsed = null,
        tokensPerSecond = null,
        responseTimeMs = null,
        timestamp = Clock.System.now().toEpochMilliseconds(),
    )

    private fun successEntryFor(
        placeholder: ChatEntry,
        result: ChatResult,
    ) = placeholder.copy(
        type = ChatEntryType.SUCCESS_RESPONSE,
        content = result.content,
        thinkingContent = result.thinkingContent,
        tokensUsed = result.tokensUsed,
        tokensPerSecond = result.tokensPerSecond,
        responseTimeMs = result.responseTimeMs,
    )

    private fun errorEntryFor(
        placeholder: ChatEntry,
        error: Throwable,
        apiKey: String?,
    ) = placeholder.copy(
        type = ChatEntryType.ERROR_RESPONSE,
        content = error.message?.redactSecrets(apiKey) ?: "Unknown error",
        thinkingContent = null,
    )

    // Transport errors can embed the request URL, and Google sends the API key as ?key=
    private fun String.redactSecrets(apiKey: String?): String {
        val withoutKey = if (apiKey.isNullOrBlank()) this else replace(apiKey, REDACTED)
        return withoutKey.replace(KEY_QUERY_PARAM, "$1$REDACTED")
    }

    private companion object {
        const val REDACTED = "***"
        val KEY_QUERY_PARAM = Regex("""([?&]key=)[^&\s\]]+""")
    }
}

private class StreamingState(
    private val thinkingEntry: ChatEntry,
    private val repository: ChatEntryRepository,
) {
    private val textBuilder = StringBuilder()
    private val thinkingBuilder = StringBuilder()
    private var lastPersistTimestamp = 0L

    suspend fun appendText(chunk: String) {
        textBuilder.append(chunk)
        persistIfDue()
    }

    suspend fun appendThinking(chunk: String) {
        thinkingBuilder.append(chunk)
        persistIfDue()
    }

    private suspend fun persistIfDue() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastPersistTimestamp < PERSIST_INTERVAL_MS) return
        lastPersistTimestamp = now
        repository.update(
            thinkingEntry.copy(
                content = textBuilder.toString(),
                thinkingContent = thinkingBuilder.toString(),
            ),
        )
    }

    private companion object {
        const val PERSIST_INTERVAL_MS = 150L
    }
}

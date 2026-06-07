package koog.chat.core.llm

import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.repository.ChatEntryRepository
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class LlmSessionManager(
    @Provided private val chatEntryRepository: ChatEntryRepository,
    @Provided private val llmService: LlmService,
    @Provided private val dispatcherSet: DispatcherSet,
) {
    private val sessionScope = CoroutineScope(SupervisorJob() + dispatcherSet.defaultDispatcher())
    private val activeChatsState = MutableStateFlow<Set<String>>(emptySet())

    val activeChats: StateFlow<Set<String>> = activeChatsState.asStateFlow()

    fun isGenerating(chatId: String): Flow<Boolean> = activeChatsState.map { it.contains(chatId) }

    fun sendMessage(
        chatId: String,
        userText: String,
        config: LlmConfig,
    ) {
        sessionScope.launch {
            executeChatTurn(chatId = chatId, userText = userText, config = config)
        }
    }

    private suspend fun executeChatTurn(
        chatId: String,
        userText: String,
        config: LlmConfig,
    ) {
        activeChatsState.update { it + chatId }
        val priorHistory = chatEntryRepository.getAll(chatId)
        val userEntry = newUserEntry(chatId = chatId, content = userText, config = config)
        val thinkingEntry = newThinkingEntry(chatId = chatId, config = config)

        try {
            chatEntryRepository.save(userEntry)
            chatEntryRepository.save(thinkingEntry)
            val promptHistory = priorHistory + userEntry
            val streamingState = StreamingState(thinkingEntry = thinkingEntry, repository = chatEntryRepository)
            val result =
                llmService.chat(
                    messages = promptHistory,
                    config = config,
                    onThinkingChunk = { chunk -> streamingState.appendThinking(chunk) },
                    onTextChunk = { chunk -> streamingState.appendText(chunk) },
                )
            chatEntryRepository.update(
                successEntryFor(
                    placeholder = thinkingEntry,
                    config = config,
                    result = result,
                ),
            )
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (error: Exception) {
            chatEntryRepository.update(
                errorEntryFor(
                    placeholder = thinkingEntry,
                    config = config,
                    error = error,
                ),
            )
        } finally {
            activeChatsState.update { it - chatId }
        }
    }

    private fun newUserEntry(
        chatId: String,
        content: String,
        config: LlmConfig,
    ) = ChatEntry(
        id = Uuid.random().toString(),
        chatId = chatId,
        type = ChatEntryType.USER_PROMPT,
        content = content,
        thinkingContent = null,
        llmConfigId = config.id,
        llmProvider = config.provider.name,
        llmModelId = config.modelId,
        tokensUsed = null,
        tokensPerSecond = null,
        responseTimeMs = null,
        timestamp = Clock.System.now().toEpochMilliseconds(),
    )

    private fun newThinkingEntry(
        chatId: String,
        config: LlmConfig,
    ) = ChatEntry(
        id = Uuid.random().toString(),
        chatId = chatId,
        type = ChatEntryType.THINKING,
        content = "",
        thinkingContent = "",
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
        config: LlmConfig,
        result: ChatResult,
    ) = placeholder.copy(
        type = ChatEntryType.SUCCESS_RESPONSE,
        content = result.content,
        thinkingContent = result.thinkingContent,
        llmConfigId = config.id,
        llmProvider = config.provider.name,
        llmModelId = config.modelId,
        tokensUsed = result.tokensUsed,
        tokensPerSecond = result.tokensPerSecond,
        responseTimeMs = result.responseTimeMs,
    )

    private fun errorEntryFor(
        placeholder: ChatEntry,
        config: LlmConfig,
        error: Throwable,
    ) = placeholder.copy(
        type = ChatEntryType.ERROR_RESPONSE,
        content = error.message ?: "Unknown error",
        thinkingContent = null,
        llmConfigId = config.id,
        llmProvider = config.provider.name,
        llmModelId = config.modelId,
    )
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

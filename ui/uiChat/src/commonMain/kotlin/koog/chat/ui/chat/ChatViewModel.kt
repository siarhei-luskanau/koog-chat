package koog.chat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.repository.ChatEntryRepository
import koog.chat.core.database.api.repository.ChatRepository
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.core.llm.LlmSessionManager
import koog.chat.core.pref.AppMode
import koog.chat.core.pref.PrefService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ChatViewModel(
    @InjectedParam private val chatId: String,
    @Provided private val navigationCallback: ChatNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val prefService: PrefService,
    @Provided private val chatEntryRepository: ChatEntryRepository,
    @Provided private val chatRepository: ChatRepository,
    @Provided private val llmConfigRepository: LlmConfigRepository,
    @Provided private val llmSessionManager: LlmSessionManager,
) : ViewModel() {
    val viewState: StateFlow<ChatViewState>
        field = MutableStateFlow<ChatViewState>(ChatViewState.Loading)

    val messagesFlow: Flow<PagingData<ChatEntry>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
            chatEntryRepository.pagingSource(chatId)
        }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            val chatTitle = chatRepository.getById(chatId)?.title ?: "Chat"
            combine(
                prefService.getAppMode(),
                llmConfigRepository.getAllFlow(),
                llmSessionManager.isGenerating(chatId),
            ) { appMode, configs, isGenerating ->
                buildSuccessState(
                    chatTitle = chatTitle,
                    appMode = appMode,
                    configs = configs,
                    isGenerating = isGenerating,
                )
            }.collect { state -> viewState.value = state }
        }
    }

    private fun buildSuccessState(
        chatTitle: String,
        appMode: AppMode,
        configs: List<LlmConfig>,
        isGenerating: Boolean,
    ): ChatViewState.Success {
        val current = viewState.value as? ChatViewState.Success
        val inputText = current?.inputText.orEmpty()
        val selectedConfigId = current?.selectedConfigId ?: configs.firstOrNull { it.isDefault }?.id
        val selectedModel = resolveSelectedModel(configs = configs, selectedConfigId = selectedConfigId)
        return ChatViewState.Success(
            chatTitle = chatTitle,
            inputText = inputText,
            isGenerating = isGenerating,
            selectedModel = selectedModel,
            selectedConfigId = selectedConfigId,
            isAdvancedMode = appMode == AppMode.Advanced,
            totalTokens = null,
        )
    }

    private fun resolveSelectedModel(
        configs: List<LlmConfig>,
        selectedConfigId: String?,
    ): String =
        configs.firstOrNull { it.id == selectedConfigId }?.modelId
            ?: configs.firstOrNull { it.isDefault }?.modelId
            ?: DEFAULT_MODEL_ID

    fun onEvent(event: ChatViewEvent) {
        viewModelScope.launch {
            when (event) {
                ChatViewEvent.NavigateBack -> navigationCallback.goBack()
                is ChatViewEvent.InputChanged -> handleInputChanged(event.text)
                ChatViewEvent.SendMessage -> handleSendMessage()
                ChatViewEvent.PickModel -> Unit
            }
        }
    }

    private fun handleInputChanged(text: String) {
        val current = viewState.value as? ChatViewState.Success ?: return
        viewState.value = current.copy(inputText = text)
    }

    private suspend fun handleSendMessage() {
        val current = viewState.value as? ChatViewState.Success ?: return
        val input = current.inputText.trim()
        if (input.isEmpty() || current.isGenerating) return
        val config = pickConfigForSend(current) ?: return
        viewState.value = current.copy(inputText = "")
        llmSessionManager.sendMessage(chatId = chatId, userText = input, config = config)
    }

    private suspend fun pickConfigForSend(current: ChatViewState.Success): LlmConfig? {
        val configs = llmConfigRepository.getAllFlow().first()
        return configs.firstOrNull { it.id == current.selectedConfigId }
            ?: configs.firstOrNull { it.isDefault }
            ?: configs.firstOrNull()
    }

    private companion object {
        const val PAGE_SIZE = 30
        const val DEFAULT_MODEL_ID = "qwen3.5:0.8b"
    }
}

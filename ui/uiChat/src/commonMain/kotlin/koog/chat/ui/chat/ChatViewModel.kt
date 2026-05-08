package koog.chat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.common.DispatcherSet
import koog.chat.core.pref.PrefService
import koog.chat.ui.common.theme.AppMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ChatViewModel(
    @InjectedParam private val initArg: String,
    @Provided private val navigationCallback: ChatNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val prefService: PrefService,
) : ViewModel() {
    val viewState: StateFlow<ChatViewState>
        field = MutableStateFlow<ChatViewState>(ChatViewState.Loading)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            prefService.getKey().collect {
                viewState.value =
                    ChatViewState.Success(
                        chatTitle = "Chat",
                        selectedModel = "qwen3.5:0.8b",
                        appMode = AppMode.Simple,
                        totalTokens = null,
                    )
            }
        }
    }

    fun onEvent(event: ChatViewEvent) {
        viewModelScope.launch {
            when (event) {
                ChatViewEvent.NavigateBack -> {
                    navigationCallback.goBack()
                }

                is ChatViewEvent.InputChanged -> {
                    val current = viewState.value
                    if (current is ChatViewState.Success) {
                        viewState.value = current.copy(inputText = event.text)
                    }
                }

                ChatViewEvent.SendMessage -> {
                    Unit
                }

                ChatViewEvent.PickModel -> {
                    Unit
                }
            }
        }
    }
}

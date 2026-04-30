package koog.chat.ui.llmconfig.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.ui.llmconfig.LlmConfigNavigationCallback
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class LlmConfigListViewModel(
    @Provided private val navigationCallback: LlmConfigNavigationCallback,
    @Provided private val repository: LlmConfigRepository,
) : ViewModel() {
    val viewState: StateFlow<LlmConfigListViewState> =
        repository
            .getAllFlow()
            .map { configs -> LlmConfigListViewState.Success(configs) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, LlmConfigListViewState.Loading)

    fun onEvent(event: LlmConfigListViewEvent) {
        viewModelScope.launch {
            when (event) {
                is LlmConfigListViewEvent.OpenDetails -> {
                    navigationCallback.openConfigDetails(event.id)
                }

                LlmConfigListViewEvent.AddNew -> {
                    navigationCallback.openConfigDetails(null)
                }

                is LlmConfigListViewEvent.Delete -> {
                    repository.delete(event.id)
                }

                LlmConfigListViewEvent.NavigateBack -> {
                    navigationCallback.goBack()
                }
            }
        }
    }
}

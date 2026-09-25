package koog.chat.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.common.DispatcherSet
import koog.chat.core.database.api.repository.LlmConfigRepository
import koog.chat.core.pref.PrefService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class MainViewModel(
    @InjectedParam private val initArg: String,
    @Provided private val navigationCallback: MainNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val prefService: PrefService,
    @Provided private val llmConfigRepository: LlmConfigRepository,
) : ViewModel() {
    val viewState: StateFlow<MainViewState> =
        combine(
            prefService.getKey(),
            llmConfigRepository.getAllFlow(),
        ) { pref, llmConfigs ->
            "initArg=$initArg pref=$pref llmConfigs=${llmConfigs.size}"
        }.map<String, MainViewState> { data -> MainViewState.Success(data = data) }
            .flowOn(dispatcherSet.defaultDispatcher())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = MainViewState.Loading,
            )

    fun onEvent(event: MainViewEvent) {
        viewModelScope.launch {
            when (event) {
                MainViewEvent.NavigateBack -> viewModelScope.launch { navigationCallback.goBack() }
            }
        }
    }
}

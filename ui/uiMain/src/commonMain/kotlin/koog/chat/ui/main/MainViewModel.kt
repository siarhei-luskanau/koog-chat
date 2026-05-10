package koog.chat.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import koog.chat.core.common.DispatcherSet
import koog.chat.core.pref.PrefService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
) : ViewModel() {
    val viewState: StateFlow<MainViewState>
        field = MutableStateFlow<MainViewState>(MainViewState.Loading)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            prefService.getAppMode().collect { appMode ->
                viewState.value = MainViewState.Success(data = "initArg=$initArg appMode=$appMode")
            }
        }
    }

    fun onEvent(event: MainViewEvent) {
        viewModelScope.launch {
            when (event) {
                MainViewEvent.NavigateBack -> viewModelScope.launch { navigationCallback.goBack() }
            }
        }
    }
}

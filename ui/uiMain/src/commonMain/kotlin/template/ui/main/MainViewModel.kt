package template.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import template.core.common.DispatcherSet
import template.core.database.DatabaseRepository
import template.core.pref.PrefService

@KoinViewModel
class MainViewModel(
    @InjectedParam private val initArg: String,
    @Provided private val navigationCallback: MainNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val prefService: PrefService,
    @Provided private val databaseRepository: DatabaseRepository,
) : ViewModel() {
    val viewState: StateFlow<MainViewState> =
        combine(
            prefService.getKey(),
            databaseRepository.getAll(),
        ) { pref, records ->
            "initArg=$initArg pref=$pref records=${records.size}"
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

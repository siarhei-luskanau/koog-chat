package template.ui.main

sealed interface MainViewEvent {
    data object NavigateBack : MainViewEvent
}

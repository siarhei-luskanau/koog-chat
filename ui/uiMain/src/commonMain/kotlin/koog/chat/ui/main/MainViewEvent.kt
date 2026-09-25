package koog.chat.ui.main

sealed interface MainViewEvent {
    data object NavigateBack : MainViewEvent
}

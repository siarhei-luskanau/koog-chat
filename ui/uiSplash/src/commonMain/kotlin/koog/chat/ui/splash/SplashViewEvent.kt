package koog.chat.ui.splash

sealed interface SplashViewEvent {
    data object Launched : SplashViewEvent
}

package template.ui.splash

sealed interface SplashViewEvent {
    data object Launched : SplashViewEvent
}

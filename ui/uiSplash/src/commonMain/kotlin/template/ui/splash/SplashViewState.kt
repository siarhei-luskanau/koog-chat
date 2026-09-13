package template.ui.splash

sealed interface SplashViewState {
    object Loading : SplashViewState

    data class Success(
        val data: String,
    ) : SplashViewState

    data class Error(
        val error: Throwable,
    ) : SplashViewState
}

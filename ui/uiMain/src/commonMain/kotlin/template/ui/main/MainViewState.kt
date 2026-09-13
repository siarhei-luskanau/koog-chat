package template.ui.main

sealed interface MainViewState {
    object Loading : MainViewState

    data class Success(
        val data: String,
    ) : MainViewState

    data class Error(
        val error: Throwable,
    ) : MainViewState
}

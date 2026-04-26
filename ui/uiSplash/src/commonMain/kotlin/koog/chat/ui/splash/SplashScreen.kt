package koog.chat.ui.splash

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import koog.chat.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SplashScreen(viewModel: SplashViewModel) {
    SplashContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun SplashContent(
    viewStateFlow: StateFlow<SplashViewState>,
    onEvent: (SplashViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    Scaffold {
        val text =
            when (val viewStateValue = viewState.value) {
                is SplashViewState.Error -> viewStateValue.error.message
                SplashViewState.Loading -> "Loading"
                is SplashViewState.Success -> viewStateValue.data
            }
        Text("Splash: $text")
    }
    LaunchedEffect(Unit) {
        onEvent(SplashViewEvent.Launched)
    }
}

@Preview
@Composable
internal fun SplashScreenLoadingPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Loading),
            onEvent = {},
        )
    }

@Preview
@Composable
internal fun SplashScreenSuccessPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Success("Preview")),
            onEvent = {},
        )
    }

@Preview
@Composable
internal fun SplashScreenErrorPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Error(RuntimeException("Error"))),
            onEvent = {},
        )
    }

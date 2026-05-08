package koog.chat.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
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
    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val text =
                when (val viewStateValue = viewState.value) {
                    is SplashViewState.Error -> viewStateValue.error.message
                    SplashViewState.Loading -> "Loading"
                    is SplashViewState.Success -> viewStateValue.data
                }
            Text("Splash: $text")
        }
    }
    LaunchedEffect(Unit) {
        onEvent(SplashViewEvent.Launched)
    }
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun SplashScreenLoadingPreviewLight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenLoadingPreviewNight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun SplashScreenSuccessPreviewLight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Success("Preview")),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenSuccessPreviewNight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Success("Preview")),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun SplashScreenErrorPreviewLight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Error(RuntimeException("Error"))),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenErrorPreviewNight() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Error(RuntimeException("Error"))),
            onEvent = {},
        )
    }

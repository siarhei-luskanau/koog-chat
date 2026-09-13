package template.ui.splash

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import template.ui.common.theme.AppTheme

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
@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenLoadingPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenSuccessPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Success("Preview")),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun SplashScreenErrorPreview() =
    AppTheme {
        SplashContent(
            viewStateFlow = MutableStateFlow(SplashViewState.Error(RuntimeException("Error"))),
            onEvent = {},
        )
    }

package template.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import template.ui.common.resources.Res
import template.ui.common.resources.back_button
import template.ui.common.resources.ic_arrow_back
import template.ui.common.theme.AppTheme

@Composable
fun MainScreen(viewModel: MainViewModel) {
    MainContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun MainContent(
    viewStateFlow: StateFlow<MainViewState>,
    onEvent: (MainViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Main") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MainViewEvent.NavigateBack) }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back_button),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        val text =
            when (val result = viewState.value) {
                is MainViewState.Error -> "Error: ${result.error.message}"
                MainViewState.Loading -> "Loading..."
                is MainViewState.Success -> result.data
            }
        Text(
            modifier = Modifier.padding(contentPadding),
            text = "Main: $text",
        )
    }
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun MainScreenPreview() =
    AppTheme {
        MainContent(
            viewStateFlow = MutableStateFlow(MainViewState.Success("Preview")),
            onEvent = {},
        )
    }

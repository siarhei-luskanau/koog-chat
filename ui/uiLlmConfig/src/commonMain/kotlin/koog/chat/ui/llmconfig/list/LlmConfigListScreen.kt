package koog.chat.ui.llmconfig.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.add_provider
import koog.chat.ui.common.resources.back_button
import koog.chat.ui.common.resources.default_badge
import koog.chat.ui.common.resources.delete_provider
import koog.chat.ui.common.resources.ic_add
import koog.chat.ui.common.resources.ic_arrow_back
import koog.chat.ui.common.resources.ic_delete
import koog.chat.ui.common.resources.llm_providers
import koog.chat.ui.common.resources.no_providers_yet
import koog.chat.ui.common.theme.AppTheme
import koog.chat.ui.common.theme.KoogShapes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LlmConfigListScreen(viewModel: LlmConfigListViewModel) {
    LlmConfigListContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun LlmConfigListContent(
    viewStateFlow: StateFlow<LlmConfigListViewState>,
    onEvent: (LlmConfigListViewEvent) -> Unit,
) {
    val viewState by viewStateFlow.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(Res.string.llm_providers)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(LlmConfigListViewEvent.NavigateBack) }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back_button),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(Res.string.add_provider)) },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_add),
                        contentDescription = null,
                    )
                },
                onClick = { onEvent(LlmConfigListViewEvent.AddNew) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { contentPadding ->
        when (val state = viewState) {
            LlmConfigListViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is LlmConfigListViewState.Success -> {
                if (state.configs.isEmpty()) {
                    LlmConfigListEmpty(modifier = Modifier.fillMaxSize().padding(contentPadding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                    ) {
                        items(state.configs, key = { it.id }) { config ->
                            LlmConfigRow(
                                config = config,
                                onClick = { onEvent(LlmConfigListViewEvent.OpenDetails(config.id)) },
                                onDelete = { onEvent(LlmConfigListViewEvent.Delete(config.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LlmConfigRow(
    config: LlmConfig,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = config.provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (config.isDefault) {
                    DefaultBadge()
                }
            }
            Text(
                text = config.modelId,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_delete),
                contentDescription = stringResource(Res.string.delete_provider),
            )
        }
    }
}

@Composable
private fun DefaultBadge() {
    Surface(
        shape = KoogShapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = stringResource(Res.string.default_badge),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun LlmConfigListEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.no_providers_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewConfigs =
    listOf(
        LlmConfig(
            id = "1",
            provider = LlmProvider.Ollama,
            modelId = "qwen3.5:0.8b",
            apiKey = null,
            providerUrl = null,
            isDefault = true,
        ),
        LlmConfig(
            id = "2",
            provider = LlmProvider.Ollama,
            modelId = "gpt-oss:20b",
            apiKey = null,
            providerUrl = "http://localhost:11434",
            isDefault = false,
        ),
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun LlmConfigListScreenSuccessPreviewLight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Success(previewConfigs)),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun LlmConfigListScreenSuccessPreviewNight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Success(previewConfigs)),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun LlmConfigListScreenEmptyPreviewLight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Success(emptyList())),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun LlmConfigListScreenEmptyPreviewNight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Success(emptyList())),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun LlmConfigListScreenLoadingPreviewLight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun LlmConfigListScreenLoadingPreviewNight() =
    AppTheme {
        LlmConfigListContent(
            viewStateFlow = MutableStateFlow(LlmConfigListViewState.Loading),
            onEvent = {},
        )
    }

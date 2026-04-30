package koog.chat.ui.llmconfig.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.api_key
import koog.chat.ui.common.resources.back_button
import koog.chat.ui.common.resources.delete_provider
import koog.chat.ui.common.resources.edit_provider
import koog.chat.ui.common.resources.ic_arrow_back
import koog.chat.ui.common.resources.ic_delete
import koog.chat.ui.common.resources.model_id
import koog.chat.ui.common.resources.new_provider
import koog.chat.ui.common.resources.provider
import koog.chat.ui.common.resources.provider_url
import koog.chat.ui.common.resources.save
import koog.chat.ui.common.resources.set_as_default
import koog.chat.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LlmConfigDetailsScreen(viewModel: LlmConfigDetailsViewModel) {
    LlmConfigDetailsContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun LlmConfigDetailsContent(
    viewStateFlow: StateFlow<LlmConfigDetailsViewState>,
    onEvent: (LlmConfigDetailsViewEvent) -> Unit,
) {
    val state by viewStateFlow.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(stringResource(if (state.isNew) Res.string.new_provider else Res.string.edit_provider))
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(LlmConfigDetailsViewEvent.NavigateBack) }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back_button),
                        )
                    }
                },
                actions = {
                    if (state.canDelete) {
                        IconButton(onClick = { onEvent(LlmConfigDetailsViewEvent.Delete) }) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_delete),
                                contentDescription = stringResource(Res.string.delete_provider),
                            )
                        }
                    }
                    TextButton(
                        onClick = { onEvent(LlmConfigDetailsViewEvent.Save) },
                        enabled = state.canSave,
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        ) {
            ProviderDropdown(
                selectedProvider = state.provider,
                onProviderSelected = { onEvent(LlmConfigDetailsViewEvent.ProviderChanged(it)) },
            )
            OutlinedTextField(
                value = state.modelId,
                onValueChange = { onEvent(LlmConfigDetailsViewEvent.ModelIdChanged(it)) },
                label = { Text(stringResource(Res.string.model_id)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { onEvent(LlmConfigDetailsViewEvent.ApiKeyChanged(it)) },
                label = { Text(stringResource(Res.string.api_key)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.providerUrl,
                onValueChange = { onEvent(LlmConfigDetailsViewEvent.ProviderUrlChanged(it)) },
                label = { Text(stringResource(Res.string.provider_url)) },
                placeholder = { Text(PROVIDER_URL_PLACEHOLDER) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.set_as_default),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = state.isDefault,
                    onCheckedChange = { onEvent(LlmConfigDetailsViewEvent.ToggleDefault) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderDropdown(
    selectedProvider: LlmProvider,
    onProviderSelected: (LlmProvider) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        OutlinedTextField(
            value = selectedProvider.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.provider)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            LlmProvider.entries.forEach { candidate ->
                DropdownMenuItem(
                    text = { Text(candidate.name) },
                    onClick = {
                        onProviderSelected(candidate)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

private const val PROVIDER_URL_PLACEHOLDER = "http://localhost:11434"

private val previewNewState = LlmConfigDetailsViewState()

private val previewEditingState =
    LlmConfigDetailsViewState(
        provider = LlmProvider.Ollama,
        modelId = "qwen3.5:0.8b",
        apiKey = "secret-key",
        providerUrl = "http://localhost:11434",
        isDefault = true,
        isNew = false,
        canDelete = true,
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun LlmConfigDetailsScreenNewPreviewLight() =
    AppTheme {
        LlmConfigDetailsContent(
            viewStateFlow = MutableStateFlow(previewNewState),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun LlmConfigDetailsScreenNewPreviewNight() =
    AppTheme {
        LlmConfigDetailsContent(
            viewStateFlow = MutableStateFlow(previewNewState),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun LlmConfigDetailsScreenEditingPreviewLight() =
    AppTheme {
        LlmConfigDetailsContent(
            viewStateFlow = MutableStateFlow(previewEditingState),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun LlmConfigDetailsScreenEditingPreviewNight() =
    AppTheme {
        LlmConfigDetailsContent(
            viewStateFlow = MutableStateFlow(previewEditingState),
            onEvent = {},
        )
    }

package koog.chat.ui.chat

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import koog.chat.core.database.api.entity.ChatEntry
import koog.chat.core.database.api.entity.ChatEntryType
import koog.chat.core.database.api.entity.LlmConfig
import koog.chat.core.database.api.entity.LlmProvider
import koog.chat.ui.common.components.AssistantBubble
import koog.chat.ui.common.components.ErrorBubble
import koog.chat.ui.common.components.InputBar
import koog.chat.ui.common.components.Metrics
import koog.chat.ui.common.components.ThinkingBlock
import koog.chat.ui.common.components.TotalTokensChip
import koog.chat.ui.common.components.UserBubble
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.back_button
import koog.chat.ui.common.resources.ic_arrow_back
import koog.chat.ui.common.resources.select_model
import koog.chat.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    ChatContent(
        viewStateFlow = viewModel.viewState,
        messagesFlow = viewModel.messagesFlow,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ChatContent(
    viewStateFlow: StateFlow<ChatViewState>,
    messagesFlow: Flow<PagingData<ChatEntry>>,
    onEvent: (ChatViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    val state = viewState.value

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(if (state is ChatViewState.Success) state.chatTitle else "Chat")
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ChatViewEvent.NavigateBack) }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back_button),
                        )
                    }
                },
                actions = {
                    if (state is ChatViewState.Success && state.isAdvancedMode && state.totalTokens != null) {
                        Box(Modifier.padding(end = AppTheme.spacing.sm)) {
                            TotalTokensChip(totalTokens = state.totalTokens)
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        when (state) {
            is ChatViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChatViewState.Success -> {
                ChatSuccessContent(
                    state = state,
                    messagesFlow = messagesFlow,
                    onEvent = onEvent,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            is ChatViewState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorBubble(
                        message = state.error.message ?: "Unknown error",
                        modifier = Modifier.padding(AppTheme.spacing.lg),
                    )
                }
            }
        }
        if (state is ChatViewState.Success && state.isModelPickerVisible) {
            ModelPickerBottomSheet(
                configs = state.availableConfigs,
                selectedConfigId = state.selectedConfigId,
                onSelect = { onEvent(ChatViewEvent.SelectModel(it)) },
                onDismiss = { onEvent(ChatViewEvent.PickModel) },
            )
        }
    }
}

@Composable
private fun ChatSuccessContent(
    state: ChatViewState.Success,
    messagesFlow: Flow<PagingData<ChatEntry>>,
    onEvent: (ChatViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val lazyMessages = messagesFlow.collectAsLazyPagingItems()

    LaunchedEffect(lazyMessages.itemCount) {
        if (lazyMessages.itemCount > 0) {
            listState.animateScrollToItem(lazyMessages.itemCount - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding =
                PaddingValues(
                    horizontal = AppTheme.spacing.lg,
                    vertical = AppTheme.spacing.sm,
                ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.bubbleGap),
        ) {
            items(
                count = lazyMessages.itemCount,
                key = lazyMessages.itemKey { it.id },
            ) { index ->
                val entry = lazyMessages[index] ?: return@items
                ChatEntryItem(entry = entry, isAdvancedMode = state.isAdvancedMode, isGenerating = state.isGenerating)
            }
        }
        InputBar(
            value = state.inputText,
            onValueChange = { onEvent(ChatViewEvent.InputChanged(it)) },
            selectedModel = state.selectedModel,
            onPickModel = { onEvent(ChatViewEvent.PickModel) },
            onSend = { onEvent(ChatViewEvent.SendMessage) },
            modifier =
                Modifier.padding(
                    horizontal = AppTheme.spacing.lg,
                    vertical = AppTheme.spacing.sm,
                ),
        )
    }
}

@Composable
private fun ChatEntryItem(
    entry: ChatEntry,
    isAdvancedMode: Boolean,
    isGenerating: Boolean,
) {
    when (entry.type) {
        ChatEntryType.USER_PROMPT -> {
            UserBubble(text = entry.content)
        }

        ChatEntryType.THINKING -> {
            ThinkingBlock(
                content = entry.thinkingContent.orEmpty(),
                isStreaming = isGenerating,
                isAdvancedMode = isAdvancedMode,
            )
        }

        ChatEntryType.SUCCESS_RESPONSE -> {
            AssistantBubble(
                text = entry.content,
                metrics = entry.toMetricsOrNull(),
                isAdvancedMode = isAdvancedMode,
            )
        }

        ChatEntryType.ERROR_RESPONSE -> {
            ErrorBubble(message = entry.content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPickerBottomSheet(
    configs: List<LlmConfig>,
    selectedConfigId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(Res.string.select_model),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            configs.forEach { config ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(config.id) }
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = config.id == selectedConfigId,
                        onClick = { onSelect(config.id) },
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = config.modelId,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = config.provider.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun ChatEntry.toMetricsOrNull(): Metrics? {
    val tokens = tokensUsed
    val tps = tokensPerSecond
    val time = responseTimeMs
    if (tokens == null || tps == null || time == null) return null
    return Metrics(
        responseTimeMs = time,
        tokensPerSecond = tps.toFloat(),
        tokensUsed = tokens.toInt(),
    )
}

private const val PREVIEW_CHAT_ID = "preview"
private const val PREVIEW_PROVIDER = "Ollama"
private const val PREVIEW_MODEL_ID = "qwen3.5:0.8b"

private val previewEntries =
    listOf(
        ChatEntry(
            id = "1",
            chatId = PREVIEW_CHAT_ID,
            type = ChatEntryType.USER_PROMPT,
            content = "How does Compose Multiplatform work?",
            thinkingContent = null,
            llmConfigId = null,
            llmProvider = PREVIEW_PROVIDER,
            llmModelId = PREVIEW_MODEL_ID,
            tokensUsed = null,
            tokensPerSecond = null,
            responseTimeMs = null,
            timestamp = 1L,
        ),
        ChatEntry(
            id = "2",
            chatId = PREVIEW_CHAT_ID,
            type = ChatEntryType.THINKING,
            content = "",
            thinkingContent = "The user is asking about Compose Multiplatform architecture.\nI'll explain the shared UI approach.",
            llmConfigId = null,
            llmProvider = PREVIEW_PROVIDER,
            llmModelId = PREVIEW_MODEL_ID,
            tokensUsed = null,
            tokensPerSecond = null,
            responseTimeMs = null,
            timestamp = 2L,
        ),
        ChatEntry(
            id = "3",
            chatId = PREVIEW_CHAT_ID,
            type = ChatEntryType.SUCCESS_RESPONSE,
            content = "Compose Multiplatform lets you share UI code across Android, iOS, desktop, and web using Kotlin.",
            thinkingContent = null,
            llmConfigId = null,
            llmProvider = PREVIEW_PROVIDER,
            llmModelId = PREVIEW_MODEL_ID,
            tokensUsed = 512L,
            tokensPerSecond = 38.0,
            responseTimeMs = 1420L,
            timestamp = 3L,
        ),
        ChatEntry(
            id = "4",
            chatId = PREVIEW_CHAT_ID,
            type = ChatEntryType.USER_PROMPT,
            content = "How do I handle theme colours?",
            thinkingContent = null,
            llmConfigId = null,
            llmProvider = PREVIEW_PROVIDER,
            llmModelId = PREVIEW_MODEL_ID,
            tokensUsed = null,
            tokensPerSecond = null,
            responseTimeMs = null,
            timestamp = 4L,
        ),
        ChatEntry(
            id = "5",
            chatId = PREVIEW_CHAT_ID,
            type = ChatEntryType.ERROR_RESPONSE,
            content = "Connection to Ollama timed out. Retrying…",
            thinkingContent = null,
            llmConfigId = null,
            llmProvider = PREVIEW_PROVIDER,
            llmModelId = PREVIEW_MODEL_ID,
            tokensUsed = null,
            tokensPerSecond = null,
            responseTimeMs = null,
            timestamp = 5L,
        ),
    )

private val previewMessagesFlow: Flow<PagingData<ChatEntry>> = flowOf(PagingData.from(previewEntries))

private fun successState(isAdvancedMode: Boolean) =
    ChatViewState.Success(
        chatTitle = "Compose Multiplatform",
        selectedModel = PREVIEW_MODEL_ID,
        selectedConfigId = null,
        isAdvancedMode = isAdvancedMode,
        totalTokens = 1284,
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenSimplePreviewLight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(successState(isAdvancedMode = false)),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenSimplePreviewNight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(successState(isAdvancedMode = false)),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenAdvancedPreviewLight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(successState(isAdvancedMode = true)),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenAdvancedPreviewNight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(successState(isAdvancedMode = true)),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenLoadingPreviewLight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(ChatViewState.Loading),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenLoadingPreviewNight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(ChatViewState.Loading),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

private val previewConfigs =
    listOf(
        LlmConfig(id = "1", provider = LlmProvider.Ollama, modelId = "qwen3.5:0.8b", apiKey = null, providerUrl = null, isDefault = true),
        LlmConfig(id = "2", provider = LlmProvider.Ollama, modelId = "gpt-oss:20b", apiKey = null, providerUrl = null, isDefault = false),
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenModelPickerPreviewLight() =
    AppTheme {
        ChatContent(
            viewStateFlow =
                MutableStateFlow(
                    successState(isAdvancedMode = false).copy(
                        isModelPickerVisible = true,
                        availableConfigs = previewConfigs,
                        selectedConfigId = "1",
                    ),
                ),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenModelPickerPreviewNight() =
    AppTheme {
        ChatContent(
            viewStateFlow =
                MutableStateFlow(
                    successState(isAdvancedMode = false).copy(
                        isModelPickerVisible = true,
                        availableConfigs = previewConfigs,
                        selectedConfigId = "1",
                    ),
                ),
            messagesFlow = previewMessagesFlow,
            onEvent = {},
        )
    }

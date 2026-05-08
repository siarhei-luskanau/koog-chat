package koog.chat.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
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
import koog.chat.ui.common.theme.AppMode
import koog.chat.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    ChatContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ChatContent(
    viewStateFlow: StateFlow<ChatViewState>,
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
                    if (state is ChatViewState.Success &&
                        state.appMode == AppMode.Advanced &&
                        state.totalTokens != null
                    ) {
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
    }
}

@Composable
private fun ChatSuccessContent(
    state: ChatViewState.Success,
    onEvent: (ChatViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
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
            items(state.messages, key = { it.id }) { message ->
                when (message.type) {
                    ChatMessageType.USER -> {
                        UserBubble(text = message.content)
                    }

                    ChatMessageType.THINKING -> {
                        ThinkingBlock(
                            content = message.content,
                            isStreaming = message.isStreaming,
                            appMode = state.appMode,
                        )
                    }

                    ChatMessageType.ASSISTANT -> {
                        AssistantBubble(
                            text = message.content,
                            metrics = message.metrics,
                            appMode = state.appMode,
                        )
                    }

                    ChatMessageType.ERROR -> {
                        ErrorBubble(message = message.content)
                    }
                }
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

private val previewMessages =
    listOf(
        ChatMessage(
            id = "1",
            type = ChatMessageType.USER,
            content = "How does Compose Multiplatform work?",
        ),
        ChatMessage(
            id = "2",
            type = ChatMessageType.THINKING,
            content = "The user is asking about Compose Multiplatform architecture.\nI'll explain the shared UI approach.",
            isStreaming = false,
        ),
        ChatMessage(
            id = "3",
            type = ChatMessageType.ASSISTANT,
            content = "Compose Multiplatform lets you share UI code across Android, iOS, desktop, and web using Kotlin.",
            metrics = Metrics(responseTimeMs = 1420, tokensPerSecond = 38f, tokensUsed = 512),
        ),
        ChatMessage(
            id = "4",
            type = ChatMessageType.USER,
            content = "How do I handle theme colours?",
        ),
        ChatMessage(
            id = "5",
            type = ChatMessageType.ERROR,
            content = "Connection to Ollama timed out. Retrying…",
        ),
    )

private fun successState(appMode: AppMode) =
    ChatViewState.Success(
        chatTitle = "Compose Multiplatform",
        messages = previewMessages,
        selectedModel = "qwen3.5:0.8b",
        appMode = appMode,
        totalTokens = 1284,
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenSimplePreviewLight() =
    AppTheme {
        ChatContent(viewStateFlow = MutableStateFlow(successState(appMode = AppMode.Simple)), onEvent = {})
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenSimplePreviewNight() =
    AppTheme {
        ChatContent(viewStateFlow = MutableStateFlow(successState(appMode = AppMode.Simple)), onEvent = {})
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenAdvancedPreviewLight() =
    AppTheme {
        ChatContent(viewStateFlow = MutableStateFlow(successState(appMode = AppMode.Advanced)), onEvent = {})
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenAdvancedPreviewNight() =
    AppTheme {
        ChatContent(viewStateFlow = MutableStateFlow(successState(appMode = AppMode.Advanced)), onEvent = {})
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatScreenLoadingPreviewLight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(ChatViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatScreenLoadingPreviewNight() =
    AppTheme {
        ChatContent(
            viewStateFlow = MutableStateFlow(ChatViewState.Loading),
            onEvent = {},
        )
    }

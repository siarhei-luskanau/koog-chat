package koog.chat.ui.chatlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.ui.common.components.ChatListItem
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.app_name
import koog.chat.ui.common.resources.ic_add
import koog.chat.ui.common.resources.ic_chat
import koog.chat.ui.common.resources.ic_search
import koog.chat.ui.common.resources.ic_settings
import koog.chat.ui.common.resources.new_chat
import koog.chat.ui.common.resources.no_chats_yet
import koog.chat.ui.common.resources.search_chats
import koog.chat.ui.common.resources.settings
import koog.chat.ui.common.resources.start_conversation
import koog.chat.ui.common.theme.AppTheme
import koog.chat.ui.common.theme.KoogShapes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatListScreen(viewModel: ChatListViewModel) {
    ChatListContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ChatListContent(
    viewStateFlow: StateFlow<ChatListViewState>,
    onEvent: (ChatListViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()

    Scaffold(
        topBar = {
            ChatListTopBar(
                onSearchClick = { onEvent(ChatListViewEvent.ToggleSearch) },
                onSettingsClick = {},
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(Res.string.new_chat)) },
                icon = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_add),
                        contentDescription = null,
                    )
                },
                onClick = { onEvent(ChatListViewEvent.NewChat) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { contentPadding ->
        when (val state = viewState.value) {
            ChatListViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChatListViewState.Success -> {
                ChatListSuccessContent(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            is ChatListViewState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(AppTheme.spacing.lg),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListSuccessContent(
    state: ChatListViewState.Success,
    onEvent: (ChatListViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = state.isSearchVisible) {
            SearchField(
                query = state.searchQuery,
                onQueryChange = { onEvent(ChatListViewEvent.SearchQueryChanged(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
            )
        }

        if (state.groups.isEmpty()) {
            ChatListEmpty(
                onNewChat = { onEvent(ChatListViewEvent.NewChat) },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                state.groups.forEach { group ->
                    item(key = "header_${group.label}") {
                        GroupHeader(label = group.label)
                    }
                    items(group.items, key = { it.id }) { entry ->
                        ChatListItem(
                            title = entry.title,
                            timestamp = entry.timestamp,
                            preview = entry.preview,
                            modelName = entry.modelName,
                            messageCount = entry.messageCount,
                            avatarColorIndex = entry.avatarColorIndex,
                            onClick = { onEvent(ChatListViewEvent.OpenChat(entry.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.search_chats)) },
        leadingIcon = {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_search),
                contentDescription = null,
            )
        },
        shape = RoundedCornerShape(28.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        singleLine = true,
        modifier = modifier.height(56.dp),
    )
}

@Composable
private fun GroupHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier
                .padding(horizontal = 14.dp)
                .padding(top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun ChatListEmpty(
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Surface(
                shape = KoogShapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_chat),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.no_chats_yet),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.start_conversation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNewChat,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(Res.string.new_chat))
            }
        }
    }
}

private val previewGroups =
    listOf(
        ChatDateGroup(
            label = "Today",
            items =
                listOf(
                    ChatListEntry(
                        id = "1",
                        title = "Compose design system",
                        timestamp = "14:32",
                        preview = "How do I set up Material3 with custom tokens in Compose Multiplatform?",
                        modelName = "claude-3-5-sonnet",
                        messageCount = 12,
                        avatarColorIndex = 1,
                    ),
                    ChatListEntry(
                        id = "2",
                        title = "Kotlin coroutines",
                        timestamp = "11:05",
                        preview = "Explain the difference between launch and async in Kotlin coroutines.",
                        modelName = "claude-3-haiku",
                        messageCount = 8,
                        avatarColorIndex = 0,
                    ),
                ),
        ),
        ChatDateGroup(
            label = "Yesterday",
            items =
                listOf(
                    ChatListEntry(
                        id = "3",
                        title = "KMP build setup",
                        timestamp = "Yesterday",
                        preview = "How to configure Gradle for Kotlin Multiplatform with iOS and Android targets?",
                        modelName = "claude-3-5-haiku",
                        messageCount = 5,
                        avatarColorIndex = 2,
                    ),
                ),
        ),
    )

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenSuccessPreviewLight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Success(groups = previewGroups)),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenSuccessPreviewNight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Success(groups = previewGroups)),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenEmptyPreviewLight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Success(groups = emptyList())),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenEmptyPreviewNight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Success(groups = emptyList())),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenLoadingPreviewLight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Loading),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenLoadingPreviewNight() =
    AppTheme {
        ChatListContent(
            viewStateFlow = MutableStateFlow(ChatListViewState.Loading),
            onEvent = {},
        )
    }

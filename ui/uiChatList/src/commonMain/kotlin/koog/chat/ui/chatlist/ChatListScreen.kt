package koog.chat.ui.chatlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import koog.chat.core.pref.AppMode
import koog.chat.ui.common.components.ChatListItem
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.app_mode
import koog.chat.ui.common.resources.ic_add
import koog.chat.ui.common.resources.ic_chat
import koog.chat.ui.common.resources.ic_search
import koog.chat.ui.common.resources.mode_advanced
import koog.chat.ui.common.resources.mode_simple
import koog.chat.ui.common.resources.new_chat
import koog.chat.ui.common.resources.no_chats_yet
import koog.chat.ui.common.resources.search_chats
import koog.chat.ui.common.resources.start_conversation
import koog.chat.ui.common.theme.AppTheme
import koog.chat.ui.common.theme.KoogShapes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ChatListScreen(viewModel: ChatListViewModel) {
    ChatListContent(
        pagingDataFlow = viewModel.pagingDataFlow,
        isSearchVisible = viewModel.isSearchVisible,
        searchQuery = viewModel.searchQuery,
        isSettingsVisible = viewModel.isSettingsVisible,
        currentAppMode = viewModel.currentAppMode,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ChatListContent(
    pagingDataFlow: Flow<PagingData<ChatPagingItem>>,
    isSearchVisible: StateFlow<Boolean>,
    searchQuery: StateFlow<String>,
    isSettingsVisible: StateFlow<Boolean>,
    currentAppMode: StateFlow<AppMode>,
    onEvent: (ChatListViewEvent) -> Unit,
) {
    val lazyPagingItems = pagingDataFlow.collectAsLazyPagingItems()
    val isSearchShown by isSearchVisible.collectAsState()
    val query by searchQuery.collectAsState()
    val isSettingsShown by isSettingsVisible.collectAsState()
    val appMode by currentAppMode.collectAsState()

    Scaffold(
        topBar = {
            ChatListTopBar(
                onSearchClick = { onEvent(ChatListViewEvent.ToggleSearch) },
                onSettingsClick = { onEvent(ChatListViewEvent.ToggleSettings) },
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
        when {
            lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            lazyPagingItems.loadState.refresh is LoadState.Error -> {
                val error = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = error.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(AppTheme.spacing.lg),
                    )
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                    AnimatedVisibility(visible = isSearchShown) {
                        SearchField(
                            query = query,
                            onQueryChange = { onEvent(ChatListViewEvent.SearchQueryChanged(it)) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 8.dp),
                        )
                    }

                    if (lazyPagingItems.itemCount == 0 &&
                        lazyPagingItems.loadState.refresh is LoadState.NotLoading
                    ) {
                        ChatListEmpty(
                            onNewChat = { onEvent(ChatListViewEvent.NewChat) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            items(lazyPagingItems.itemCount) { index ->
                                when (val item = lazyPagingItems[index]) {
                                    is ChatPagingItem.Header -> {
                                        GroupHeader(label = item.label)
                                    }

                                    is ChatPagingItem.Entry -> {
                                        ChatListItem(
                                            title = item.chat.title,
                                            timestamp = item.chat.timestamp,
                                            preview = item.chat.preview,
                                            modelName = item.chat.modelName,
                                            messageCount = item.chat.messageCount,
                                            avatarColorIndex = item.chat.avatarColorIndex,
                                            onClick = { onEvent(ChatListViewEvent.OpenChat(item.chat.id)) },
                                        )
                                    }

                                    null -> {
                                        Unit
                                    }
                                }
                            }
                            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (isSettingsShown) {
        SettingsBottomSheet(
            currentMode = appMode,
            onModeSelected = { onEvent(ChatListViewEvent.SetAppMode(it)) },
            onDismiss = { onEvent(ChatListViewEvent.ToggleSettings) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    currentMode: AppMode,
    onModeSelected: (AppMode) -> Unit,
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
                text = stringResource(Res.string.app_mode),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            AppMode.entries.forEach { mode ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = mode == currentMode,
                        onClick = { onModeSelected(mode) },
                    )
                    Text(
                        text =
                            stringResource(
                                when (mode) {
                                    AppMode.Simple -> Res.string.mode_simple
                                    AppMode.Advanced -> Res.string.mode_advanced
                                },
                            ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
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
        }
    }
}

private val previewSuccessItems =
    Clock.System.now().let { now ->
        listOf(
            ChatPagingItem.Header("Today"),
            ChatPagingItem.Entry(
                chat =
                    ChatListEntry(
                        id = "1",
                        title = "Compose design system",
                        timestamp = "14:32",
                        preview = "How do I set up Material3 with custom tokens in Compose Multiplatform?",
                        modelName = "claude-3-5-sonnet",
                        messageCount = 12,
                        avatarColorIndex = 1,
                    ),
                createdAt = now,
            ),
            ChatPagingItem.Entry(
                chat =
                    ChatListEntry(
                        id = "2",
                        title = "Kotlin coroutines",
                        timestamp = "11:05",
                        preview = "Explain the difference between launch and async in Kotlin coroutines.",
                        modelName = "claude-3-haiku",
                        messageCount = 8,
                        avatarColorIndex = 0,
                    ),
                createdAt = now,
            ),
            ChatPagingItem.Header("Yesterday"),
            ChatPagingItem.Entry(
                chat =
                    ChatListEntry(
                        id = "3",
                        title = "KMP build setup",
                        timestamp = "Yesterday",
                        preview = "How to configure Gradle for Kotlin Multiplatform with iOS and Android targets?",
                        modelName = "claude-3-5-haiku",
                        messageCount = 5,
                        avatarColorIndex = 2,
                    ),
                createdAt = now.minus(86400000.milliseconds),
            ),
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenSuccessPreviewLight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow = flowOf(PagingData.from(previewSuccessItems)),
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenSuccessPreviewNight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow = flowOf(PagingData.from(previewSuccessItems)),
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenEmptyPreviewLight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow =
                flowOf(
                    PagingData.from(
                        data = emptyList(),
                        sourceLoadStates =
                            LoadStates(
                                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                                append = LoadState.NotLoading(endOfPaginationReached = false),
                            ),
                    ),
                ),
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenEmptyPreviewNight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow =
                flowOf(
                    PagingData.from(
                        data = emptyList(),
                        sourceLoadStates =
                            LoadStates(
                                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                                append = LoadState.NotLoading(endOfPaginationReached = false),
                            ),
                    ),
                ),
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListScreenLoadingPreviewLight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow = flow { },
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListScreenLoadingPreviewNight() =
    AppTheme {
        ChatListContent(
            pagingDataFlow = flow { },
            isSearchVisible = MutableStateFlow(false),
            searchQuery = MutableStateFlow(""),
            isSettingsVisible = MutableStateFlow(false),
            currentAppMode = MutableStateFlow(AppMode.Simple),
            onEvent = {},
        )
    }

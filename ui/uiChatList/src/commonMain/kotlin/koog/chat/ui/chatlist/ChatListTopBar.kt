package koog.chat.ui.chatlist

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.app_name
import koog.chat.ui.common.resources.ic_search
import koog.chat.ui.common.resources.ic_settings
import koog.chat.ui.common.resources.search_chats
import koog.chat.ui.common.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatListTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.search_chats),
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_settings),
                    contentDescription = stringResource(Res.string.settings),
                )
            }
        },
    )
}

package koog.chat.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import koog.chat.ui.common.theme.AppTheme
import koog.chat.ui.common.theme.KoogShapes

@Composable
fun ChatListItem(
    title: String,
    timestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: String = "",
    modelName: String? = null,
    messageCount: Int? = null,
    avatarColorIndex: Int = 0,
) {
    val avatarPairs =
        listOf(
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        )
    val (avatarBg, avatarFg) = avatarPairs[avatarColorIndex % avatarPairs.size]
    val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(KoogShapes.medium)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    ),
                color = avatarFg,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                Text(
                    text = timestamp,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (preview.isNotEmpty()) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (modelName != null || messageCount != null) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (modelName != null) {
                        ModelChip(modelName = modelName)
                    }
                    if (messageCount != null) {
                        CountChip(count = messageCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelChip(modelName: String) {
    Surface(
        shape = KoogShapes.extraLarge,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            text = modelName,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                ),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun CountChip(count: Int) {
    Surface(
        shape = KoogShapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = "$count msgs",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatListItemPreviewLight() =
    AppTheme {
        ChatListItem(
            title = "Compose design system",
            timestamp = "Today 14:32",
            preview = "How do I set up Material3 with custom tokens in Compose Multiplatform?",
            modelName = "claude-3-5-sonnet",
            messageCount = 12,
            avatarColorIndex = 1,
            onClick = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatListItemPreviewNight() =
    AppTheme {
        ChatListItem(
            title = "Compose design system",
            timestamp = "Today 14:32",
            preview = "How do I set up Material3 with custom tokens in Compose Multiplatform?",
            modelName = "claude-3-5-sonnet",
            messageCount = 12,
            avatarColorIndex = 1,
            onClick = {},
        )
    }

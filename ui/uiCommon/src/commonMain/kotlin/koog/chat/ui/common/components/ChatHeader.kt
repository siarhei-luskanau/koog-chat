package koog.chat.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.ui.common.theme.AppTheme

@Composable
fun ChatHeader(
    title: String,
    subtitle: String? = null,
    totalTokens: Int? = null,
    modifier: Modifier = Modifier,
    isAdvancedMode: Boolean,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    top = AppTheme.spacing.xl,
                    bottom = AppTheme.spacing.md,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isAdvancedMode && totalTokens != null) {
            TotalTokensChip(totalTokens)
        }
    }
}

@Composable
fun TotalTokensChip(
    totalTokens: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.height(AppTheme.spacing.chipHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Text(
                text = "Σ ${formatTokenCount(totalTokens)} tokens",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatHeaderSimplePreviewLight() =
    AppTheme { ChatHeader(title = "Compose tokens", subtitle = "qwen3.5:0.8b · Ollama", isAdvancedMode = false) }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatHeaderSimplePreviewNight() =
    AppTheme { ChatHeader(title = "Compose tokens", subtitle = "qwen3.5:0.8b · Ollama", isAdvancedMode = false) }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ChatHeaderAdvancedPreviewLight() =
    AppTheme {
        ChatHeader(
            title = "Compose tokens",
            subtitle = "qwen3.5:0.8b · Ollama",
            totalTokens = 1284,
            isAdvancedMode = true,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatHeaderAdvancedPreviewNight() =
    AppTheme {
        ChatHeader(
            title = "Compose tokens",
            subtitle = "qwen3.5:0.8b · Ollama",
            totalTokens = 1284,
            isAdvancedMode = true,
        )
    }

private fun formatTokenCount(count: Int): String =
    if (count >= 1000) {
        val thousands = count / 1000
        val remainder = (count % 1000) / 100
        if (remainder == 0) "${thousands}k" else "$thousands.${remainder}k"
    } else {
        count.toString()
    }

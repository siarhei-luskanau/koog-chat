package koog.chat.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.ui.common.theme.AppTheme

@Immutable
data class Metrics(
    val responseTimeMs: Long,
    val tokensPerSecond: Float,
    val tokensUsed: Int,
)

@Composable
fun MetricsRow(metrics: Metrics) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val style =
            MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        Text(formatResponseTime(metrics.responseTimeMs), style = style)
        Text("·", style = style)
        Text("${metrics.tokensPerSecond.toInt()} tok/s", style = style)
        Text("·", style = style)
        Text("${metrics.tokensUsed} tokens", style = style)
    }
}

private fun formatResponseTime(ms: Long): String {
    val seconds = ms / 1000
    val fraction = (ms % 1000) / 100
    return "$seconds.${fraction}s"
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun MetricsRowPreviewLight() = AppTheme { MetricsRow(Metrics(responseTimeMs = 1420, tokensPerSecond = 38.5f, tokensUsed = 512)) }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun MetricsRowPreviewNight() = AppTheme { MetricsRow(Metrics(responseTimeMs = 1420, tokensPerSecond = 38.5f, tokensUsed = 512)) }

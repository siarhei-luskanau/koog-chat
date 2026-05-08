package koog.chat.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.reasoning_label
import koog.chat.ui.common.resources.thinking_label
import koog.chat.ui.common.theme.AppMode
import koog.chat.ui.common.theme.AppTheme
import koog.chat.ui.common.theme.AssistantBubbleShape
import koog.chat.ui.common.theme.ThinkingBubbleShape
import koog.chat.ui.common.theme.UserBubbleShape
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = AppTheme.spacing.bubbleSideGap),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            shape = UserBubbleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
fun AssistantBubble(
    text: String,
    metrics: Metrics? = null,
    modifier: Modifier = Modifier,
    appMode: AppMode,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(end = AppTheme.spacing.bubbleSideGap),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                shape = AssistantBubbleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
            if (appMode == AppMode.Advanced && metrics != null) {
                MetricsRow(metrics)
            }
        }
    }
}

@Composable
fun ThinkingBlock(
    content: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    appMode: AppMode,
) {
    if (appMode == AppMode.Simple && !isStreaming) return
    Surface(
        shape = ThinkingBubbleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier.padding(end = AppTheme.spacing.bubbleSideGap),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isStreaming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    text =
                        if (appMode == AppMode.Simple) {
                            stringResource(Res.string.thinking_label)
                        } else {
                            stringResource(Res.string.reasoning_label)
                        },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            if (appMode == AppMode.Advanced) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun ErrorBubble(
    message: String,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.errorContainer
    val borderColor = MaterialTheme.colorScheme.error
    val onBackground = MaterialTheme.colorScheme.onErrorContainer
    Box(
        modifier =
            modifier
                .padding(end = AppTheme.spacing.bubbleSideGap)
                .dashedBorder(color = borderColor, cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(background),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = onBackground,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
): Modifier =
    drawBehind {
        val strokePx = 1.dp.toPx()
        val cornerPx = cornerRadius.toPx()
        val dashPx = 8.dp.toPx()
        val gapPx = 4.dp.toPx()
        val path =
            Path().apply {
                addRoundRect(
                    RoundRect(
                        left = strokePx / 2,
                        top = strokePx / 2,
                        right = size.width - strokePx / 2,
                        bottom = size.height - strokePx / 2,
                        radiusX = cornerPx,
                        radiusY = cornerPx,
                    ),
                )
            }
        drawPath(
            path = path,
            color = color,
            style =
                Stroke(
                    width = strokePx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx)),
                ),
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun UserBubblePreviewLight() = AppTheme { UserBubble(text = "Hello! How does Compose Multiplatform work?") }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun UserBubblePreviewNight() = AppTheme { UserBubble(text = "Hello! How does Compose Multiplatform work?") }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun AssistantBubbleSimplePreviewLight() =
    AppTheme {
        AssistantBubble(
            text = "Put Color, Type, Shape and Theme in commonMain. Use expect/actual only for fonts.",
            appMode = AppMode.Simple,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun AssistantBubbleSimplePreviewNight() =
    AppTheme {
        AssistantBubble(
            text = "Put Color, Type, Shape and Theme in commonMain. Use expect/actual only for fonts.",
            appMode = AppMode.Simple,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun AssistantBubbleAdvancedPreviewLight() =
    AppTheme {
        AssistantBubble(
            text = "Put Color, Type, Shape and Theme in commonMain.",
            metrics = Metrics(responseTimeMs = 1420, tokensPerSecond = 38f, tokensUsed = 512),
            appMode = AppMode.Advanced,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun AssistantBubbleAdvancedPreviewNight() =
    AppTheme {
        AssistantBubble(
            text = "Put Color, Type, Shape and Theme in commonMain.",
            metrics = Metrics(responseTimeMs = 1420, tokensPerSecond = 38f, tokensUsed = 512),
            appMode = AppMode.Advanced,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ThinkingBlockStreamingPreviewLight() = AppTheme { ThinkingBlock(content = "", isStreaming = true, appMode = AppMode.Simple) }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ThinkingBlockStreamingPreviewNight() = AppTheme { ThinkingBlock(content = "", isStreaming = true, appMode = AppMode.Simple) }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ThinkingBlockAdvancedPreviewLight() =
    AppTheme {
        ThinkingBlock(
            content = "The user is asking about Compose Multiplatform.\nI'll explain the theme file structure.",
            isStreaming = false,
            appMode = AppMode.Advanced,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ThinkingBlockAdvancedPreviewNight() =
    AppTheme {
        ThinkingBlock(
            content = "The user is asking about Compose Multiplatform.\nI'll explain the theme file structure.",
            isStreaming = false,
            appMode = AppMode.Advanced,
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun ErrorBubblePreviewLight() = AppTheme { ErrorBubble(message = "Connection to Ollama timed out. Retrying…") }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun ErrorBubblePreviewNight() = AppTheme { ErrorBubble(message = "Connection to Ollama timed out. Retrying…") }

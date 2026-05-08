package koog.chat.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import koog.chat.ui.common.resources.Res
import koog.chat.ui.common.resources.ic_send
import koog.chat.ui.common.resources.message_placeholder
import koog.chat.ui.common.resources.send_button
import koog.chat.ui.common.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    selectedModel: String,
    onPickModel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = AppTheme.spacing.inputBarHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.message_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            LlmSelectorChip(label = selectedModel, onClick = onPickModel)
            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(40.dp),
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_send),
                    contentDescription = stringResource(Res.string.send_button),
                )
            }
        }
    }
}

@Composable
fun LlmSelectorChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        colors =
            AssistChipDefaults.assistChipColors(
                labelColor = MaterialTheme.colorScheme.secondary,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    )
}

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun InputBarEmptyPreviewLight() =
    AppTheme {
        InputBar(
            value = "",
            onValueChange = {},
            selectedModel = "qwen3.5:0.8b",
            onPickModel = {},
            onSend = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun InputBarEmptyPreviewNight() =
    AppTheme {
        InputBar(
            value = "",
            onValueChange = {},
            selectedModel = "qwen3.5:0.8b",
            onPickModel = {},
            onSend = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
internal fun InputBarFilledPreviewLight() =
    AppTheme {
        InputBar(
            value = "How does AppTheme work?",
            onValueChange = {},
            selectedModel = "qwen3.5:0.8b",
            onPickModel = {},
            onSend = {},
        )
    }

@Preview(uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
internal fun InputBarFilledPreviewNight() =
    AppTheme {
        InputBar(
            value = "How does AppTheme work?",
            onValueChange = {},
            selectedModel = "qwen3.5:0.8b",
            onPickModel = {},
            onSend = {},
        )
    }

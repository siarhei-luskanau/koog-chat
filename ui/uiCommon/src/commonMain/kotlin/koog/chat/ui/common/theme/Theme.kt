package koog.chat.ui.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    Surface {
        CompositionLocalProvider(
            LocalSpacing provides Spacing(),
        ) {
            MaterialTheme(
                colorScheme = if (useDarkTheme) DarkColors else LightColors,
                typography = KoogTypography,
                shapes = KoogShapes,
                content = content,
            )
        }
    }
}

@Composable
private fun ColorRow(
    label: String,
    background: Color,
    content: Color,
) {
    Surface(
        color = background,
        contentColor = content,
        modifier = Modifier.fillMaxWidth().height(40.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview
@Composable
internal fun AppThemeColorsPreview() {
    AppTheme {
        Row {
            listOf("Light" to LightColors, "Dark" to DarkColors).forEach { (name, colorScheme) ->
                Column(modifier = Modifier.width(160.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(12.dp),
                    )
                    ColorRow("primary", colorScheme.primary, colorScheme.onPrimary)
                    ColorRow("primaryContainer", colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
                    ColorRow("secondary", colorScheme.secondary, colorScheme.onSecondary)
                    ColorRow("secondaryContainer", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
                    ColorRow("tertiary", colorScheme.tertiary, colorScheme.onTertiary)
                    ColorRow("tertiaryContainer", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
                    ColorRow("error", colorScheme.error, colorScheme.onError)
                    ColorRow("errorContainer", colorScheme.errorContainer, colorScheme.onErrorContainer)
                    ColorRow("surface", colorScheme.surface, colorScheme.onSurface)
                    ColorRow("surfaceVariant", colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

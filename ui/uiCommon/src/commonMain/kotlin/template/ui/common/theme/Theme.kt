package template.ui.common.theme

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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val LightColorScheme =
    lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        error = md_theme_light_error,
        onError = md_theme_light_onError,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        error = md_theme_dark_error,
        onError = md_theme_dark_onError,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
    )

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content:
        @Composable () -> Unit,
) {
    val colorScheme =
        if (!useDarkTheme) {
            LightColorScheme
        } else {
            DarkColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            Surface(content = content)
        },
    )
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
            listOf("Light" to LightColorScheme, "Dark" to DarkColorScheme).forEach { (name, colorScheme) ->
                Column(modifier = Modifier.width(160.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(12.dp),
                    )
                    ColorRow("primary", colorScheme.primary, colorScheme.onPrimary)
                    ColorRow(
                        "primaryContainer",
                        colorScheme.primaryContainer,
                        colorScheme.onPrimaryContainer,
                    )
                    ColorRow("secondary", colorScheme.secondary, colorScheme.onSecondary)
                    ColorRow(
                        "secondaryContainer",
                        colorScheme.secondaryContainer,
                        colorScheme.onSecondaryContainer,
                    )
                    ColorRow("tertiary", colorScheme.tertiary, colorScheme.onTertiary)
                    ColorRow(
                        "tertiaryContainer",
                        colorScheme.tertiaryContainer,
                        colorScheme.onTertiaryContainer,
                    )
                    ColorRow("error", colorScheme.error, colorScheme.onError)
                    ColorRow(
                        "errorContainer",
                        colorScheme.errorContainer,
                        colorScheme.onErrorContainer,
                    )
                    ColorRow("surface", colorScheme.surface, colorScheme.onSurface)
                    ColorRow(
                        "surfaceVariant",
                        colorScheme.surfaceVariant,
                        colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

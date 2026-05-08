package koog.chat.ui.common.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object KoogBrand {
    val Indigo = Color(0xFF5C6BC0)
    val DeepIndigo = Color(0xFF283593)
    val IndigoDark = Color(0xFFBEC2FF)
    val DeepIndigoDark = Color(0xFF8C8FEB)
}

val LightColors =
    lightColorScheme(
        primary = Color(0xFF1A73E8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD3E3FD),
        onPrimaryContainer = Color(0xFF041E49),
        secondary = Color(0xFF5C6BC0),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8EAFF),
        onSecondaryContainer = Color(0xFF0D1259),
        tertiary = Color(0xFF7B5EA7),
        tertiaryContainer = Color(0xFFEADDFF),
        onTertiaryContainer = Color(0xFF21005D),
        error = Color(0xFFB00020),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44464F),
        outline = Color(0xFF757680),
    )

val DarkColors =
    darkColorScheme(
        primary = Color(0xFF94BFFF),
        onPrimary = Color(0xFF00317A),
        primaryContainer = Color(0xFF004096),
        onPrimaryContainer = Color(0xFFD6E3FF),
        secondary = Color(0xFFBEC2FF),
        onSecondary = Color(0xFF262396),
        secondaryContainer = Color(0xFF3C3F9F),
        onSecondaryContainer = Color(0xFFE2E0FF),
        tertiary = Color(0xFFD4BAFF),
        tertiaryContainer = Color(0xFF5B3E8C),
        onTertiaryContainer = Color(0xFFEADDFF),
        error = Color(0xFFFFB4A9),
        errorContainer = Color(0xFF930006),
        onErrorContainer = Color(0xFFFFDAD6),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF44464F),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099),
    )

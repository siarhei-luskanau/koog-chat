package koog.chat.ui.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val bubbleGap: Dp = 8.dp,
    val bubbleSideGap: Dp = 64.dp,
    val inputBarHeight: Dp = 56.dp,
    val chipHeight: Dp = 32.dp,
    val metricsRowHeight: Dp = 24.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

object AppTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current
}

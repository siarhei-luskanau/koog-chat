package koog.chat.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

internal sealed interface AppRoutes : NavKey {
    @Serializable
    data object Splash : AppRoutes

    @Serializable
    data object Main : AppRoutes

    @Serializable
    data class Chat(
        val chatId: String,
    ) : AppRoutes
}

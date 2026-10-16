import androidx.compose.ui.window.ComposeViewport
import koog.chat.di.KoinApp

internal fun main() {
    installPromptApiSpike()
    ComposeViewport { KoinApp() }
}

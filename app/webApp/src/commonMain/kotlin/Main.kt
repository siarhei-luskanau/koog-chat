import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import template.di.KoinApp

@OptIn(ExperimentalComposeUiApi::class)
internal fun main() = ComposeViewport { KoinApp() }

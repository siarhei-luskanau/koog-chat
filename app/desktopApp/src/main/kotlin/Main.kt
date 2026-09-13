import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import template.di.KoinApp
import java.awt.Dimension

internal fun main() =
    application {
        Window(
            title = "compose-multiplatform-template",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            @Suppress("MagicNumber")
            window.minimumSize = Dimension(350, 600)
            KoinApp()
        }
    }

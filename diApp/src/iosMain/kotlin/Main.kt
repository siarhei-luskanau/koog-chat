import androidx.compose.ui.window.ComposeUIViewController
import koog.chat.di.KoinApp
import platform.UIKit.UIViewController

fun mainViewController(): UIViewController =
    ComposeUIViewController {
        KoinApp()
    }

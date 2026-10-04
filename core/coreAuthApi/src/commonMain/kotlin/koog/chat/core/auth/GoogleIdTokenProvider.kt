package koog.chat.core.auth

import androidx.compose.runtime.Composable

interface GoogleIdTokenProvider {
    @Composable
    fun rememberGoogleSignInLauncher(onResult: (Result<String>) -> Unit): GoogleSignInLauncher
}

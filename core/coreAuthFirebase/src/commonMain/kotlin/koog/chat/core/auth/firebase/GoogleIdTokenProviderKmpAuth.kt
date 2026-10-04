package koog.chat.core.auth.firebase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.mmk.kmpauth.core.KMPAuth
import com.mmk.kmpauth.google.GoogleUser
import com.mmk.kmpauth.google.google
import com.mmk.kmpauth.google.rememberGoogleSignInState
import koog.chat.core.auth.GoogleIdTokenProvider
import koog.chat.core.auth.GoogleSignInLauncher
import org.koin.core.annotation.Single

@Single
internal class GoogleIdTokenProviderKmpAuth : GoogleIdTokenProvider {
    private val isConfigured = GoogleAuthConfig.WEB_CLIENT_ID.isNotBlank()

    init {
        if (isConfigured) {
            KMPAuth.initialize { google(serverId = GoogleAuthConfig.WEB_CLIENT_ID) }
        }
    }

    @Composable
    override fun rememberGoogleSignInLauncher(onResult: (Result<String>) -> Unit): GoogleSignInLauncher {
        val currentOnResult by rememberUpdatedState(onResult)
        if (!isConfigured) {
            return remember { GoogleSignInLauncher { currentOnResult(Result.failure(MissingWebClientIdException())) } }
        }
        val signInState =
            rememberGoogleSignInState(
                onResult = { result -> currentOnResult(result.map(GoogleUser::idToken)) },
            )
        return remember(signInState) { GoogleSignInLauncher(signInState::launch) }
    }
}

private class MissingWebClientIdException :
    IllegalStateException("GOOGLE_WEB_CLIENT_ID is not set in local.properties; see docs/setup-firebase.md")

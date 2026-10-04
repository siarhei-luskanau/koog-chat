package koog.chat.core.auth.fake

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import koog.chat.core.auth.GoogleIdTokenProvider
import koog.chat.core.auth.GoogleSignInLauncher
import org.koin.core.annotation.Single

private const val FAKE_GOOGLE_ID_TOKEN = "fake-google-id-token"

@Single
internal class GoogleIdTokenProviderFake : GoogleIdTokenProvider {
    @Composable
    override fun rememberGoogleSignInLauncher(onResult: (Result<String>) -> Unit): GoogleSignInLauncher {
        val currentOnResult by rememberUpdatedState(onResult)
        return remember { GoogleSignInLauncher { currentOnResult(Result.success(FAKE_GOOGLE_ID_TOKEN)) } }
    }
}

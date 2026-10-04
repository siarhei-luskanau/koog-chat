package koog.chat.di

import androidx.compose.ui.test.v2.runComposeUiTest
import koog.chat.core.auth.AuthService
import koog.chat.core.auth.GoogleIdTokenProvider
import koog.chat.core.auth.GoogleSignInLauncher
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoogleIdTokenProviderCommonTest {
    private val koinApplication = koinApplication<DiKoinApplication>()

    @AfterTest
    fun tearDown() {
        koinApplication.close()
    }

    @Test
    fun launcherDeliversFakeGoogleIdToken() =
        runComposeUiTest {
            val provider = koinApplication.koin.get<GoogleIdTokenProvider>()
            val results = mutableListOf<Result<String>>()
            lateinit var launcher: GoogleSignInLauncher
            setContent {
                launcher = provider.rememberGoogleSignInLauncher { results += it }
            }
            waitForIdle()

            launcher.launch()

            assertEquals(listOf(Result.success(FAKE_GOOGLE_ID_TOKEN)), results)
        }

    @Test
    fun fakeGoogleIdTokenSignsInFakeUser() =
        runTest {
            val service = koinApplication.koin.get<AuthService>()

            val user = service.signInWithGoogleIdToken(FAKE_GOOGLE_ID_TOKEN).getOrThrow()

            assertEquals("fake-uid-$FAKE_GOOGLE_ID_TOKEN", user.uid)
        }

    private companion object {
        const val FAKE_GOOGLE_ID_TOKEN = "fake-google-id-token"
    }
}

package koog.chat.core.auth.fake

import androidx.compose.ui.test.v2.runComposeUiTest
import koog.chat.core.auth.GoogleSignInLauncher
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoogleIdTokenProviderFakeCommonTest {
    @Test
    fun launch_shouldReturnFakeIdToken() =
        runComposeUiTest {
            var launcher: GoogleSignInLauncher? = null
            var result: Result<String>? = null
            setContent {
                launcher = GoogleIdTokenProviderFake().rememberGoogleSignInLauncher { result = it }
            }
            waitForIdle()

            runOnIdle { launcher?.launch() }

            assertEquals("fake-google-id-token", result?.getOrThrow())
        }
}

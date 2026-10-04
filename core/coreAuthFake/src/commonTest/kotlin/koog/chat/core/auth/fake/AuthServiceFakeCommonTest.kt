package koog.chat.core.auth.fake

import koog.chat.core.auth.AuthUser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AuthServiceFakeCommonTest {
    private val authService = AuthServiceFake()

    @Test
    fun currentUser_shouldBeNull_whenNotSignedIn() {
        assertNull(authService.currentUser.value)
    }

    @Test
    fun signInWithGoogleIdToken_shouldEmitFakeUser_whenTokenIsProvided() =
        runTest {
            val expected =
                AuthUser(
                    uid = "fake-uid-token",
                    displayName = "Fake User",
                    email = "fake.user@example.com",
                    photoUrl = null,
                )

            val result = authService.signInWithGoogleIdToken("token")

            assertEquals(expected, result.getOrThrow())
            assertEquals(expected, authService.currentUser.value)
        }

    @Test
    fun signOut_shouldClearCurrentUser_whenSignedIn() =
        runTest {
            authService.signInWithGoogleIdToken("token")

            authService.signOut()

            assertNull(authService.currentUser.value)
        }
}

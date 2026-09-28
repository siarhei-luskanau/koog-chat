package koog.chat.core.auth.fake

import koog.chat.core.auth.AuthService
import koog.chat.core.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
internal class AuthServiceFake : AuthService {
    override val currentUser: StateFlow<AuthUser?>
        field = MutableStateFlow<AuthUser?>(null)

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser> {
        val authUser =
            AuthUser(
                uid = "fake-uid-$idToken",
                displayName = "Fake User",
                email = "fake.user@example.com",
                photoUrl = null,
            )
        currentUser.value = authUser
        return Result.success(authUser)
    }

    override suspend fun signOut() {
        currentUser.value = null
    }
}

package koog.chat.core.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser>

    suspend fun signOut()
}

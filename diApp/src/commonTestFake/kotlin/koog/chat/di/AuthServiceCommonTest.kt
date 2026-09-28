package koog.chat.di

import koog.chat.core.auth.AuthService
import koog.chat.core.auth.AuthUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthServiceCommonTest {
    private val koinApplication = koinApplication<DiKoinApplication>()
    private lateinit var service: AuthService

    @BeforeTest
    fun setUp() {
        service = koinApplication.koin.get<AuthService>()
    }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
    }

    @Test
    fun koinBindsAuthServiceAsSingleton() {
        assertSame(service, koinApplication.koin.get<AuthService>())
    }

    @Test
    fun startsSignedOut() =
        runTest {
            val emissions = collectCurrentUser()

            assertEquals(listOf<AuthUser?>(null), emissions)
        }

    @Test
    fun signInCreatesUser() =
        runTest {
            val emissions = collectCurrentUser()

            val user = service.signInWithGoogleIdToken("any-id-token").getOrThrow()

            assertEquals(FAKE_USER, user)
            assertEquals(listOf(null, FAKE_USER), emissions)
        }

    @Test
    fun signOutClearsUser() =
        runTest {
            val emissions = collectCurrentUser()

            service.signInWithGoogleIdToken("any-id-token")
            service.signOut()

            assertEquals(listOf(null, FAKE_USER, null), emissions)
        }

    @Test
    fun signOutWhenSignedOutEmitsNothingNew() =
        runTest {
            val emissions = collectCurrentUser()

            service.signOut()

            assertEquals(listOf<AuthUser?>(null), emissions)
        }

    @Test
    fun signInAgainWithOtherTokenEmitsNewUser() =
        runTest {
            val emissions = collectCurrentUser()

            service.signInWithGoogleIdToken("any-id-token")
            service.signOut()
            val otherUser = service.signInWithGoogleIdToken("other-id-token").getOrThrow()

            assertEquals("fake-uid-other-id-token", otherUser.uid)
            assertEquals(listOf(null, FAKE_USER, null, otherUser), emissions)
        }

    private fun TestScope.collectCurrentUser(): List<AuthUser?> {
        val emissions = mutableListOf<AuthUser?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            service.currentUser.toList(emissions)
        }
        return emissions
    }

    private companion object {
        val FAKE_USER =
            AuthUser(
                uid = "fake-uid-any-id-token",
                displayName = "Fake User",
                email = "fake.user@example.com",
                photoUrl = null,
            )
    }
}

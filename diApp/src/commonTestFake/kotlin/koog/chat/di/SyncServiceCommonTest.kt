package koog.chat.di

import koog.chat.core.auth.AuthService
import koog.chat.core.sync.SyncService
import koog.chat.core.sync.SyncState
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
internal class SyncServiceCommonTest {
    private val koinApplication = koinApplication<DiKoinApplication>()
    private lateinit var service: SyncService

    @BeforeTest
    fun setUp() {
        service = koinApplication.koin.get<SyncService>()
    }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
    }

    @Test
    fun koinBindsSyncServiceAsSingleton() {
        assertSame(service, koinApplication.koin.get<SyncService>())
    }

    @Test
    fun startsIdle() =
        runTest {
            val emissions = collectSyncState()

            assertEquals(listOf<SyncState>(SyncState.Idle), emissions)
        }

    @Test
    fun startEmitsSyncingAndStopReturnsToIdle() =
        runTest {
            val emissions = collectSyncState()

            service.start()
            service.stop()

            assertEquals(listOf(SyncState.Idle, SyncState.Syncing, SyncState.Idle), emissions)
        }

    @Test
    fun repeatedStartEmitsSyncingOnce() =
        runTest {
            val emissions = collectSyncState()

            service.start()
            service.start()

            assertEquals(listOf(SyncState.Idle, SyncState.Syncing), emissions)
        }

    @Test
    fun startSyncsWithoutSignIn() =
        runTest {
            val emissions = collectSyncState()

            service.start()

            assertEquals(
                null,
                koinApplication.koin
                    .get<AuthService>()
                    .currentUser.value,
            )
            assertEquals(listOf(SyncState.Idle, SyncState.Syncing), emissions)
        }

    private fun TestScope.collectSyncState(): List<SyncState> {
        val emissions = mutableListOf<SyncState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            service.syncState.toList(emissions)
        }
        return emissions
    }
}

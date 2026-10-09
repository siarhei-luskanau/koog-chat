package koog.chat.core.sync.fake

import koog.chat.core.sync.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SyncServiceFakeCommonTest {
    private val syncService = SyncServiceFake()

    @Test
    fun syncState_shouldBeIdle_whenCreated() {
        assertEquals(SyncState.Idle, syncService.syncState.value)
    }

    @Test
    fun start_shouldSetSyncing_whenCalled() {
        syncService.start()

        assertEquals(SyncState.Syncing, syncService.syncState.value)
    }

    @Test
    fun stop_shouldSetIdle_whenStartedBefore() {
        syncService.start()
        syncService.stop()

        assertEquals(SyncState.Idle, syncService.syncState.value)
    }

    @Test
    fun stop_shouldKeepIdle_whenNotStarted() {
        syncService.stop()

        assertEquals(SyncState.Idle, syncService.syncState.value)
    }
}

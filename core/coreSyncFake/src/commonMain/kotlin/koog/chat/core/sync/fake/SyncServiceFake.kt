package koog.chat.core.sync.fake

import koog.chat.core.sync.SyncService
import koog.chat.core.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
internal class SyncServiceFake : SyncService {
    override val syncState: StateFlow<SyncState>
        field = MutableStateFlow<SyncState>(SyncState.Idle)

    override fun start() {
        syncState.value = SyncState.Syncing
    }

    override fun stop() {
        syncState.value = SyncState.Idle
    }
}

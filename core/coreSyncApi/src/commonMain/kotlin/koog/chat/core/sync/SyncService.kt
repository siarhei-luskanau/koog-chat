package koog.chat.core.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncService {
    val syncState: StateFlow<SyncState>

    fun start()

    fun stop()
}

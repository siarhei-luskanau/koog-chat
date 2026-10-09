package koog.chat.core.sync

sealed interface SyncState {
    data object Idle : SyncState

    data object Syncing : SyncState

    data class Error(
        val message: String,
    ) : SyncState
}

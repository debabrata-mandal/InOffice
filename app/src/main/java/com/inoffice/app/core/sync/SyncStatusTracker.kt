package com.inoffice.app.core.sync

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SyncStatusTracker @Inject constructor() {
    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    fun markSyncing() {
        _status.value = _status.value.copy(state = SyncState.SYNCING, errorMessage = null)
    }

    fun markQueued() {
        _status.value = _status.value.copy(state = SyncState.QUEUED, errorMessage = null)
    }

    fun markSuccess(atEpochMillis: Long = System.currentTimeMillis()) {
        _status.value =
            _status.value.copy(
                state = SyncState.IDLE,
                lastSuccessAtEpochMillis = atEpochMillis,
                errorMessage = null,
            )
    }

    fun markError(message: String?) {
        _status.value =
            _status.value.copy(
                state = SyncState.ERROR,
                errorMessage = message ?: "Sync failed. Please try again.",
            )
    }

    /** Clears error UI after a cancelled sync (e.g. screen disposed); keeps last successful sync time. */
    fun clearErrorToIdle() {
        _status.value = _status.value.copy(state = SyncState.IDLE, errorMessage = null)
    }
}

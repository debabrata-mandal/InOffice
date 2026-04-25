package com.inoffice.app.core.sync

enum class SyncState {
    IDLE,
    QUEUED,
    SYNCING,
    ERROR,
}

data class SyncStatus(
    val state: SyncState = SyncState.IDLE,
    val lastSuccessAtEpochMillis: Long? = null,
    val errorMessage: String? = null,
)

package com.inoffice.app.core.sync

data class SyncSnapshot(
    val schemaVersion: Int,
    val snapshotUpdatedAtEpochMillis: Long,
    val baseMandate: Int,
    val baseMandateUpdatedAtEpochMillis: Long,
    val dayEntries: List<SyncDayEntry>,
    val deletedEntries: List<SyncDeletedEntry>,
)

data class SyncDayEntry(
    val id: String,
    val localDate: String,
    val type: String,
    val updatedAtEpochMillis: Long,
)

data class SyncDeletedEntry(
    val localDate: String,
    val updatedAtEpochMillis: Long,
)

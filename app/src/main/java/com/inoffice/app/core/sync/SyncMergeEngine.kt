package com.inoffice.app.core.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMergeEngine @Inject constructor() {
    fun merge(
        local: SyncSnapshot,
        remote: SyncSnapshot,
    ): SyncSnapshot {
        val resolution = mutableMapOf<String, ResolvedValue>()

        local.dayEntries.forEach { entry ->
            resolution[entry.localDate] = ResolvedValue.Entry(entry)
        }
        local.deletedEntries.forEach { deleted ->
            val existing = resolution[deleted.localDate]
            if (existing == null || deleted.updatedAtEpochMillis > existing.updatedAtEpochMillis) {
                resolution[deleted.localDate] = ResolvedValue.Deleted(deleted)
            }
        }

        remote.dayEntries.forEach { remoteEntry ->
            val existing = resolution[remoteEntry.localDate]
            if (existing == null || remoteEntry.updatedAtEpochMillis > existing.updatedAtEpochMillis) {
                resolution[remoteEntry.localDate] = ResolvedValue.Entry(remoteEntry)
            }
        }
        remote.deletedEntries.forEach { remoteDeleted ->
            val existing = resolution[remoteDeleted.localDate]
            if (existing == null || remoteDeleted.updatedAtEpochMillis > existing.updatedAtEpochMillis) {
                resolution[remoteDeleted.localDate] = ResolvedValue.Deleted(remoteDeleted)
            }
        }

        val mergedEntries =
            resolution.values
                .filterIsInstance<ResolvedValue.Entry>()
                .map { it.value }
                .sortedBy { it.localDate }
        val mergedDeletedEntries =
            resolution.values
                .filterIsInstance<ResolvedValue.Deleted>()
                .map { it.value }
                .sortedBy { it.localDate }

        val useRemoteMandate = remote.baseMandateUpdatedAtEpochMillis > local.baseMandateUpdatedAtEpochMillis
        return SyncSnapshot(
            schemaVersion = maxOf(local.schemaVersion, remote.schemaVersion),
            snapshotUpdatedAtEpochMillis = maxOf(local.snapshotUpdatedAtEpochMillis, remote.snapshotUpdatedAtEpochMillis),
            baseMandate = if (useRemoteMandate) remote.baseMandate else local.baseMandate,
            baseMandateUpdatedAtEpochMillis =
                maxOf(local.baseMandateUpdatedAtEpochMillis, remote.baseMandateUpdatedAtEpochMillis),
            dayEntries = mergedEntries,
            deletedEntries = mergedDeletedEntries,
        )
    }
}

private sealed interface ResolvedValue {
    val updatedAtEpochMillis: Long

    data class Entry(val value: SyncDayEntry) : ResolvedValue {
        override val updatedAtEpochMillis: Long = value.updatedAtEpochMillis
    }

    data class Deleted(val value: SyncDeletedEntry) : ResolvedValue {
        override val updatedAtEpochMillis: Long = value.updatedAtEpochMillis
    }
}

package com.inoffice.app.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.inoffice.app.core.data.local.DayEntryDao
import com.inoffice.app.core.data.local.DayEntryEntity
import com.inoffice.app.core.data.repo.DataStoreMandatePreferences
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DriveSyncCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dayEntryDao: DayEntryDao,
    private val deletionStore: SyncDeletionStore,
    private val mandatePreferences: DataStoreMandatePreferences,
    private val driveClient: DriveAppDataClient,
    private val codec: SyncSnapshotJsonCodec,
    private val mergeEngine: SyncMergeEngine,
    private val localCache: LocalSnapshotCache,
    private val syncStatusTracker: SyncStatusTracker,
) : SyncTrigger {
    private val startupDone = AtomicBoolean(false)

    suspend fun runStartupSyncIfNeeded() {
        if (startupDone.getAndSet(true)) {
            return
        }
        withContext(Dispatchers.IO) {
            syncStatusTracker.markSyncing()
            runCatching {
                val remoteRaw = driveClient.downloadSnapshot()
                if (remoteRaw == null) {
                    pushLocalSnapshotInternal()
                } else {
                    val remote = codec.decode(remoteRaw)
                    val local = buildLocalSnapshot()
                    val merged = mergeEngine.merge(local, remote)
                    applyMergedSnapshot(merged)
                    localCache.write(merged)
                    syncStatusTracker.markSuccess()
                }
            }.onFailure { error ->
                syncStatusTracker.markError(error.message)
            }
        }
    }

    override fun onLocalDataChanged() {
        syncStatusTracker.markQueued()
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val request =
            OneTimeWorkRequestBuilder<DriveSyncWorker>()
                .setInitialDelay(DEBOUNCE_DELAY_SECONDS, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(UNIQUE_SYNC_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    suspend fun pushLocalSnapshot() {
        withContext(Dispatchers.IO) {
            syncStatusTracker.markSyncing()
            runCatching {
                pushLocalSnapshotInternal()
            }.onFailure { error ->
                syncStatusTracker.markError(error.message)
                throw error
            }
        }
    }

    private suspend fun pushLocalSnapshotInternal() {
        val snapshot = buildLocalSnapshot()
        val json = codec.encode(snapshot)
        localCache.write(snapshot)
        driveClient.uploadSnapshot(json)
        syncStatusTracker.markSuccess()
    }

    private suspend fun buildLocalSnapshot(): SyncSnapshot {
        val now = System.currentTimeMillis()
        deletionStore.pruneOlderThan(now - TOMBSTONE_RETENTION_MILLIS)
        val entries = dayEntryDao.getAll()
        return SyncSnapshot(
            schemaVersion = 1,
            snapshotUpdatedAtEpochMillis = now,
            baseMandate = mandatePreferences.getBaseMandateNow(),
            baseMandateUpdatedAtEpochMillis = mandatePreferences.getBaseMandateUpdatedAtNow(),
            dayEntries =
                entries.map { entity ->
                    SyncDayEntry(
                        id = entity.id,
                        localDate = entity.localDate,
                        type = entity.type,
                        updatedAtEpochMillis = entity.updatedAtEpochMillis,
                    )
                },
            deletedEntries = deletionStore.getAllDeletedEntries(),
        )
    }

    private suspend fun applyMergedSnapshot(snapshot: SyncSnapshot) {
        if (snapshot.dayEntries.isNotEmpty()) {
            dayEntryDao.upsertAll(
                snapshot.dayEntries.map { entry ->
                    DayEntryEntity(
                        localDate = entry.localDate,
                        id = entry.id,
                        type = entry.type,
                        updatedAtEpochMillis = entry.updatedAtEpochMillis,
                    )
                },
            )
        }
        snapshot.deletedEntries.forEach { deleted ->
            dayEntryDao.deleteByDate(deleted.localDate)
        }
        deletionStore.replaceAllDeletedEntries(snapshot.deletedEntries)
        deletionStore.pruneOlderThan(System.currentTimeMillis() - TOMBSTONE_RETENTION_MILLIS)
        mandatePreferences.setBaseMandateWithUpdatedAt(
            value = snapshot.baseMandate,
            updatedAtEpochMillis = snapshot.baseMandateUpdatedAtEpochMillis,
        )
    }

    companion object {
        private const val UNIQUE_SYNC_WORK_NAME = "drive_appdata_sync"
        private const val DEBOUNCE_DELAY_SECONDS = 2L
        private const val BACKOFF_DELAY_SECONDS = 10L
        private val TOMBSTONE_RETENTION_MILLIS = TimeUnit.DAYS.toMillis(90)
    }
}

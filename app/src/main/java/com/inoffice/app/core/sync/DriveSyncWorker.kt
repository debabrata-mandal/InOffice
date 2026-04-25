package com.inoffice.app.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import com.inoffice.app.di.SyncEntryPoint

class DriveSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val coordinator =
            EntryPointAccessors
                .fromApplication(applicationContext, SyncEntryPoint::class.java)
                .driveSyncCoordinator()
        return runCatching {
            coordinator.pushLocalSnapshot()
            Result.success()
        }.getOrElse { error ->
            val message = error.message.orEmpty()
            when {
                message.contains("not available", ignoreCase = true) -> Result.failure()
                message.contains("401") || message.contains("403") -> Result.failure()
                runAttemptCount >= MAX_RETRY_ATTEMPTS -> Result.failure()
                else -> Result.retry()
            }
        }
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
    }
}

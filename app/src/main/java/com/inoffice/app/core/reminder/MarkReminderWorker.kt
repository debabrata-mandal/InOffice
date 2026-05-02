package com.inoffice.app.core.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inoffice.app.core.domain.isWeekend
import com.inoffice.app.di.MarkReminderEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarkReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    MarkReminderEntryPoint::class.java,
                )
            val prefs = entryPoint.reminderPreferences()
            if (!prefs.getReminderEnabled()) {
                return@withContext Result.success()
            }
            val today = LocalDate.now()
            if (today.isWeekend()) {
                entryPoint.markReminderScheduler().reschedule()
                return@withContext Result.success()
            }
            val type = entryPoint.dayEntryRepository().getDay(today)
            if (type != null) {
                entryPoint.markReminderScheduler().reschedule()
                return@withContext Result.success()
            }
            MarkReminderNotifications.showUnmarkedReminder(applicationContext)
            entryPoint.markReminderScheduler().reschedule()
            Result.success()
        }
}

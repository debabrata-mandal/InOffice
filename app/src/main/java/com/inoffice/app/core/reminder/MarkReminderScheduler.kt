package com.inoffice.app.core.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.inoffice.app.core.domain.ReminderPreferences
import com.inoffice.app.core.domain.isWeekend
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderPreferences: ReminderPreferences,
) {
    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    suspend fun reschedule() {
        if (!reminderPreferences.getReminderEnabled()) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }
        val hour = reminderPreferences.getReminderHour()
        val minute = reminderPreferences.getReminderMinute()
        val delayMs = computeDelayUntilNextTriggerMillis(hour, minute)
        val request =
            OneTimeWorkRequestBuilder<MarkReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "mark_weekday_reminder"

        internal fun computeDelayUntilNextTriggerMillis(hour: Int, minute: Int): Long {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            val time = LocalTime.of(hour, minute)
            var date = now.toLocalDate()
            repeat(15) {
                if (!date.isWeekend()) {
                    val candidate = ZonedDateTime.of(date, time, zone)
                    if (candidate.isAfter(now)) {
                        return Duration.between(now, candidate).toMillis().coerceAtLeast(TimeUnit.SECONDS.toMillis(15))
                    }
                }
                date = date.plusDays(1)
            }
            return TimeUnit.HOURS.toMillis(24)
        }
    }
}

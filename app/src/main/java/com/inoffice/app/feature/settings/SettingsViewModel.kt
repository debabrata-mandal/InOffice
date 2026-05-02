package com.inoffice.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.MandatePreferences
import com.inoffice.app.core.domain.ReminderPreferences
import com.inoffice.app.core.reminder.MarkReminderScheduler
import com.inoffice.app.core.sync.DriveSyncCoordinator
import com.inoffice.app.core.sync.SyncTrigger
import com.inoffice.app.core.sync.SyncStatusTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mandatePreferences: MandatePreferences,
    private val reminderPreferences: ReminderPreferences,
    private val markReminderScheduler: MarkReminderScheduler,
    private val driveSyncCoordinator: DriveSyncCoordinator,
    private val syncTrigger: SyncTrigger,
    syncStatusTracker: SyncStatusTracker,
) : ViewModel() {

    val baseMandate =
        mandatePreferences.observeBaseMandate().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_BASE_MANDATE,
        )

    val reminderEnabled =
        reminderPreferences.observeReminderEnabled().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val reminderHour =
        reminderPreferences.observeReminderHour().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_REMINDER_HOUR,
        )

    val reminderMinute =
        reminderPreferences.observeReminderMinute().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_REMINDER_MINUTE,
        )

    val syncStatus =
        syncStatusTracker.status.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = syncStatusTracker.status.value,
        )

    fun saveBaseMandate(value: Int) {
        viewModelScope.launch {
            mandatePreferences.setBaseMandate(value)
            syncTrigger.onLocalDataChanged()
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderPreferences.setReminderEnabled(enabled)
            markReminderScheduler.reschedule()
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            reminderPreferences.setReminderTime(hour, minute)
            markReminderScheduler.reschedule()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            runCatching { driveSyncCoordinator.pushLocalSnapshot() }
        }
    }

    companion object {
        private const val DEFAULT_BASE_MANDATE = 12
        private const val DEFAULT_REMINDER_HOUR = 17
        private const val DEFAULT_REMINDER_MINUTE = 0
    }
}

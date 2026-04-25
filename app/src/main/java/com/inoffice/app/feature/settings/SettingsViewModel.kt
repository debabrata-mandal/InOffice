package com.inoffice.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.MandatePreferences
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

    fun syncNow() {
        viewModelScope.launch {
            runCatching { driveSyncCoordinator.pushLocalSnapshot() }
        }
    }

    companion object {
        private const val DEFAULT_BASE_MANDATE = 12
    }
}

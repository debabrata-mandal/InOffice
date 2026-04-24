package com.inoffice.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.MandatePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mandatePreferences: MandatePreferences,
) : ViewModel() {

    val baseMandate =
        mandatePreferences.observeBaseMandate().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_BASE_MANDATE,
        )

    fun saveBaseMandate(value: Int) {
        viewModelScope.launch {
            mandatePreferences.setBaseMandate(value)
        }
    }

    companion object {
        private const val DEFAULT_BASE_MANDATE = 12
    }
}

package com.inoffice.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.DayType
import com.inoffice.app.core.domain.MandatePreferences
import com.inoffice.app.core.domain.monthSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val officeDays: Int,
    val adjustedTarget: Int,
    val baseMandate: Int,
    val leaveDays: Int,
    val holidayDays: Int,
    val todayType: DayType?,
    val yearMonth: YearMonth,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dayEntryRepository: DayEntryRepository,
    private val mandatePreferences: MandatePreferences,
) : ViewModel() {

    private val currentMonth: YearMonth = YearMonth.now()

    val uiState =
        combine(
            dayEntryRepository.observeEntries(currentMonth),
            mandatePreferences.observeBaseMandate(),
        ) { entries, base ->
            val summary = monthSummary(currentMonth, entries, base)
            val today = LocalDate.now()
            DashboardUiState(
                officeDays = summary.officeDays,
                adjustedTarget = summary.adjustedTarget,
                baseMandate = summary.baseMandate,
                leaveDays = summary.leaveDays,
                holidayDays = summary.holidayDays,
                todayType = entries.firstOrNull { it.localDate == today }?.type,
                yearMonth = currentMonth,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                DashboardUiState(
                    officeDays = 0,
                    adjustedTarget = 0,
                    baseMandate = 12,
                    leaveDays = 0,
                    holidayDays = 0,
                    todayType = null,
                    yearMonth = currentMonth,
                ),
        )

    fun markToday(type: DayType) {
        viewModelScope.launch {
            dayEntryRepository.setDayType(LocalDate.now(), type)
        }
    }
}

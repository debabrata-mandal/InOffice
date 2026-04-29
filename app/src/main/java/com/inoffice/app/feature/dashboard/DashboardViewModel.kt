package com.inoffice.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.DayType
import com.inoffice.app.core.domain.MandatePreferences
import com.inoffice.app.core.domain.isWeekend
import com.inoffice.app.core.domain.monthSummary
import com.inoffice.app.core.domain.weekdaysInMonth
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
    /** Office + leave + holiday; drives mandate headline and primary progress. */
    val mandateProgressDays: Int,
    val adjustedTarget: Int,
    val baseMandate: Int,
    val leaveDays: Int,
    val holidayDays: Int,
    val wfhDays: Int,
    val leaveAndHolidayDays: Int,
    val weekdaysInMonth: Int,
    /** Mon–Fri days in the month that have any mark (office, leave, holiday, WFH). */
    val markedWeekdaysInMonth: Int,
    val todayType: DayType?,
    val yearMonth: YearMonth,
    val isTodayWeekend: Boolean,
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
            val wdTotal = weekdaysInMonth(currentMonth)
            DashboardUiState(
                officeDays = summary.officeDays,
                mandateProgressDays = summary.mandateProgressDays,
                adjustedTarget = summary.adjustedTarget,
                baseMandate = summary.baseMandate,
                leaveDays = summary.leaveDays,
                holidayDays = summary.holidayDays,
                wfhDays = summary.wfhDays,
                leaveAndHolidayDays = summary.leaveDays + summary.holidayDays,
                weekdaysInMonth = wdTotal,
                markedWeekdaysInMonth = summary.markedWeekdaySlots,
                todayType = entries.firstOrNull { it.localDate == today }?.type,
                yearMonth = currentMonth,
                isTodayWeekend = today.isWeekend(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                DashboardUiState(
                    officeDays = 0,
                    mandateProgressDays = 0,
                    adjustedTarget = 0,
                    baseMandate = 12,
                    leaveDays = 0,
                    holidayDays = 0,
                    wfhDays = 0,
                    leaveAndHolidayDays = 0,
                    weekdaysInMonth = weekdaysInMonth(currentMonth),
                    markedWeekdaysInMonth = 0,
                    todayType = null,
                    yearMonth = currentMonth,
                    isTodayWeekend = LocalDate.now().isWeekend(),
                ),
        )

    fun markToday(type: DayType) {
        val today = LocalDate.now()
        if (today.isWeekend()) {
            return
        }
        viewModelScope.launch {
            dayEntryRepository.setDayType(today, type)
        }
    }
}

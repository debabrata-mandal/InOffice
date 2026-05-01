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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    /** Mon–Fri marks this month (office + leave + holiday + WFH); weekends excluded, matching in-app marking rules. */
    val markedWeekdaysInMonth: Int,
    val todayType: DayType?,
    val yesterdayType: DayType?,
    val yearMonth: YearMonth,
    val isTodayWeekend: Boolean,
    val isYesterdayWeekend: Boolean,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dayEntryRepository: DayEntryRepository,
    private val mandatePreferences: MandatePreferences,
) : ViewModel() {

    /**
     * Bumped whenever the dashboard is shown so we re-bind [DayEntryRepository.observeEntries] to
     * [YearMonth.now()]. A fixed month would miss marks after a calendar month change while the
     * ViewModel stays alive (e.g. process not killed, navigation backstack).
     */
    private val dashboardVisibleGeneration = MutableStateFlow(0)

    /**
     * Call when the dashboard screen enters composition (or resumes) so entries and summaries use
     * the correct calendar month and a fresh Room subscription.
     */
    fun onDashboardVisible() {
        dashboardVisibleGeneration.value = dashboardVisibleGeneration.value + 1
    }

    val uiState =
        dashboardVisibleGeneration
            .flatMapLatest {
                val yearMonth = YearMonth.now()
                combine(
                    dayEntryRepository.observeEntries(yearMonth),
                    dayEntryRepository.observeEntries(yearMonth.minusMonths(1)),
                    mandatePreferences.observeBaseMandate(),
                ) { thisMonthEntries, prevMonthEntries, base ->
                    val entries = thisMonthEntries + prevMonthEntries
                    val summary = monthSummary(yearMonth, thisMonthEntries, base)
                    val today = LocalDate.now()
                    val yesterday = today.minusDays(1)
                    val wdTotal = weekdaysInMonth(yearMonth)
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
                        yesterdayType = entries.firstOrNull { it.localDate == yesterday }?.type,
                        yearMonth = yearMonth,
                        isTodayWeekend = today.isWeekend(),
                        isYesterdayWeekend = yesterday.isWeekend(),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyDashboardUiState(YearMonth.now()),
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

    companion object {
        private fun emptyDashboardUiState(yearMonth: YearMonth) =
            DashboardUiState(
                officeDays = 0,
                mandateProgressDays = 0,
                adjustedTarget = 0,
                baseMandate = 12,
                leaveDays = 0,
                holidayDays = 0,
                wfhDays = 0,
                leaveAndHolidayDays = 0,
                weekdaysInMonth = weekdaysInMonth(yearMonth),
                        markedWeekdaysInMonth = 0,
                        todayType = null,
                        yesterdayType = null,
                        yearMonth = yearMonth,
                        isTodayWeekend = LocalDate.now().isWeekend(),
                        isYesterdayWeekend = LocalDate.now().minusDays(1).isWeekend(),
                    )
    }
}

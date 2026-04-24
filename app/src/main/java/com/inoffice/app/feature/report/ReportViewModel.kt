package com.inoffice.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inoffice.app.core.domain.DayEntry
import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.MandatePreferences
import com.inoffice.app.core.domain.MonthSummary
import com.inoffice.app.core.domain.monthSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class ReportUiState(
    val yearMonth: YearMonth,
    val entries: List<DayEntry>,
    val summary: MonthSummary,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val dayEntryRepository: DayEntryRepository,
    private val mandatePreferences: MandatePreferences,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState =
        selectedMonth
            .flatMapLatest { ym ->
                combine(
                    dayEntryRepository.observeEntries(ym),
                    mandatePreferences.observeBaseMandate(),
                ) { entries, base ->
                    val sorted = entries.sortedBy { it.localDate }
                    ReportUiState(
                        yearMonth = ym,
                        entries = sorted,
                        summary = monthSummary(ym, entries, base),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue =
                    ReportUiState(
                        yearMonth = selectedMonth.value,
                        entries = emptyList(),
                        summary =
                            monthSummary(
                                selectedMonth.value,
                                emptyList(),
                                12,
                            ),
                    ),
            )

    fun goToPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }
}

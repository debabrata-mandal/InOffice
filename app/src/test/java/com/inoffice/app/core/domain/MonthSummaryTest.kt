package com.inoffice.app.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MonthSummaryTest {

    @Test
    fun adjustedTarget_subtractsLeaveAndHolidayFloorsAtZero() {
        assertEquals(9, computeAdjustedTarget(12, 2, 1))
        assertEquals(0, computeAdjustedTarget(12, 8, 4))
        assertEquals(0, computeAdjustedTarget(12, 20, 0))
    }

    @Test
    fun monthSummary_countsOnlyEntriesInMonth() {
        val ym = YearMonth.of(2024, 2)
        val entries =
            listOf(
                entry(ym.atDay(1), DayType.OFFICE),
                entry(ym.atDay(2), DayType.OFFICE),
                entry(ym.atDay(3), DayType.LEAVE),
                entry(LocalDate.of(2024, 1, 31), DayType.OFFICE),
                entry(LocalDate.of(2024, 3, 1), DayType.HOLIDAY),
            )
        val summary = monthSummary(ym, entries, baseMandate = 12)
        assertEquals(2, summary.officeDays)
        assertEquals(1, summary.leaveDays)
        assertEquals(0, summary.holidayDays)
        assertEquals(11, summary.adjustedTarget)
    }

    private fun entry(
        date: LocalDate,
        type: DayType,
    ): DayEntry =
        DayEntry(
            id = "id-${date}",
            localDate = date,
            type = type,
            updatedAt = java.time.Instant.EPOCH,
        )
}

package com.inoffice.app.core.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Mon–Fri days in [yearMonth] (excludes Saturday and Sunday). */
fun weekdaysInMonth(yearMonth: YearMonth): Int =
    (1..yearMonth.lengthOfMonth()).count {
        val dow = yearMonth.atDay(it).dayOfWeek
        dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
    }

fun LocalDate.isWeekend(): Boolean =
    dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

/**
 * Mon–Fri days in [yearMonth] marked office, leave, holiday, or WFH (weekends excluded; aligns with the app, which cannot mark Sat/Sun).
 * Matches [MonthSummary.markedWeekdaySlots].
 */
fun markedWeekdayCount(
    entries: List<DayEntry>,
    yearMonth: YearMonth,
): Int {
    val inMonth = entries.filter { YearMonth.from(it.localDate) == yearMonth }
    return inMonth.count { entry ->
        !entry.localDate.isWeekend() && entry.type != DayType.NONE
    }
}

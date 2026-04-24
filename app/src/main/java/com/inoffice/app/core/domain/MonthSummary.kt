package com.inoffice.app.core.domain

import java.time.YearMonth

data class MonthSummary(
    val yearMonth: YearMonth,
    val baseMandate: Int,
    val officeDays: Int,
    val leaveDays: Int,
    val holidayDays: Int,
) {
    val adjustedTarget: Int =
        computeAdjustedTarget(baseMandate, leaveDays, holidayDays)
}

/**
 * Policy: each leave or holiday day in the month reduces the required in-office count
 * from [baseMandate], floored at zero.
 */
fun computeAdjustedTarget(baseMandate: Int, leaveDays: Int, holidayDays: Int): Int =
    maxOf(0, baseMandate - leaveDays - holidayDays)

fun monthSummary(
    yearMonth: YearMonth,
    entries: List<DayEntry>,
    baseMandate: Int,
): MonthSummary {
    val inMonth = entries.filter { YearMonth.from(it.localDate) == yearMonth }
    return MonthSummary(
        yearMonth = yearMonth,
        baseMandate = baseMandate,
        officeDays = inMonth.count { it.type == DayType.OFFICE },
        leaveDays = inMonth.count { it.type == DayType.LEAVE },
        holidayDays = inMonth.count { it.type == DayType.HOLIDAY },
    )
}

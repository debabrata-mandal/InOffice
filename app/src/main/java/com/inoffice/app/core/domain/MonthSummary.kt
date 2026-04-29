package com.inoffice.app.core.domain

import java.time.YearMonth

data class MonthSummary(
    val yearMonth: YearMonth,
    val baseMandate: Int,
    val officeDays: Int,
    val leaveDays: Int,
    val holidayDays: Int,
    val wfhDays: Int,
    /** Mon–Fri days in [yearMonth] with a stored mark (office, leave, holiday, or WFH). */
    val markedWeekdaySlots: Int,
) {
    /**
     * Required in-office days for the month — always the same as [baseMandate] from settings.
     */
    val adjustedTarget: Int get() = baseMandate

    /**
     * Counts toward the main mandate progress bar and headline: physical office plus leave and holiday
     * (leave and holiday advance progress the same as an in-office mark). WFH is excluded.
     */
    val mandateProgressDays: Int get() = officeDays + leaveDays + holidayDays
}

fun monthSummary(
    yearMonth: YearMonth,
    entries: List<DayEntry>,
    baseMandate: Int,
): MonthSummary {
    val inMonth = entries.filter { YearMonth.from(it.localDate) == yearMonth }
    val markedWeekdaySlots =
        inMonth.count { entry ->
            !entry.localDate.isWeekend() && entry.type != DayType.NONE
        }
    return MonthSummary(
        yearMonth = yearMonth,
        baseMandate = baseMandate,
        officeDays = inMonth.count { it.type == DayType.OFFICE },
        leaveDays = inMonth.count { it.type == DayType.LEAVE },
        holidayDays = inMonth.count { it.type == DayType.HOLIDAY },
        wfhDays = inMonth.count { it.type == DayType.WFH },
        markedWeekdaySlots = markedWeekdaySlots,
    )
}

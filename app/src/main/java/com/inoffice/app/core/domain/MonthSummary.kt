package com.inoffice.app.core.domain

import java.time.YearMonth

data class MonthSummary(
    val yearMonth: YearMonth,
    val baseMandate: Int,
    val officeDays: Int,
    val leaveDays: Int,
    val holidayDays: Int,
    val wfhDays: Int,
    /**
     * Mon–Fri days in [yearMonth] with office, leave, holiday, or WFH (matches in-app marking: weekends are not markable).
     * Same as weekday-only office + leave + holiday + WFH counts.
     */
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
    val officeDays = inMonth.count { it.type == DayType.OFFICE }
    val leaveDays = inMonth.count { it.type == DayType.LEAVE }
    val holidayDays = inMonth.count { it.type == DayType.HOLIDAY }
    val wfhDays = inMonth.count { it.type == DayType.WFH }
    val markedWeekdaySlots =
        inMonth.count { entry ->
            !entry.localDate.isWeekend() && entry.type != DayType.NONE
        }
    return MonthSummary(
        yearMonth = yearMonth,
        baseMandate = baseMandate,
        officeDays = officeDays,
        leaveDays = leaveDays,
        holidayDays = holidayDays,
        wfhDays = wfhDays,
        markedWeekdaySlots = markedWeekdaySlots,
    )
}

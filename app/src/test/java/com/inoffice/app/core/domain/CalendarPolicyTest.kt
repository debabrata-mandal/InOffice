package com.inoffice.app.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class CalendarPolicyTest {

    @Test
    fun weekdaysInMonth_february2024_leapYear_countsMonFriOnly() {
        assertEquals(21, weekdaysInMonth(YearMonth.of(2024, 2)))
    }

    @Test
    fun weekdaysInMonth_april2026_has22Weekdays() {
        assertEquals(22, weekdaysInMonth(YearMonth.of(2026, 4)))
    }

    @Test
    fun isWeekend_saturdayAndSunday() {
        assertTrue(LocalDate.of(2026, 4, 25).isWeekend())
        assertTrue(LocalDate.of(2026, 4, 26).isWeekend())
        assertFalse(LocalDate.of(2026, 4, 27).isWeekend())
    }

    @Test
    fun markedWeekdayCount_countsOnlyWeekdayTypedMarksInMonth() {
        val ym = YearMonth.of(2024, 2)
        val entries =
            listOf(
                DayEntry("1", ym.atDay(1), DayType.OFFICE, Instant.EPOCH),
                DayEntry("2", ym.atDay(2), DayType.WFH, Instant.EPOCH),
                DayEntry("3", ym.atDay(5), DayType.LEAVE, Instant.EPOCH),
                DayEntry("sat", ym.atDay(3), DayType.OFFICE, Instant.EPOCH),
                DayEntry("o", LocalDate.of(2024, 1, 31), DayType.OFFICE, Instant.EPOCH),
            )
        assertEquals(3, markedWeekdayCount(entries, ym))
    }

    @Test
    fun markedWeekdayCount_excludesWeekendRowsEvenIfTyped() {
        val ym = YearMonth.of(2024, 2)
        val sat = LocalDate.of(2024, 2, 3)
        assertTrue(sat.isWeekend())
        val entries = listOf(DayEntry("s", sat, DayType.OFFICE, Instant.EPOCH))
        assertEquals(0, markedWeekdayCount(entries, ym))
    }

    @Test
    fun markedWeekdayCount_includesHolidayOnWeekday() {
        val ym = YearMonth.of(2026, 4)
        val mon = LocalDate.of(2026, 4, 6)
        assertFalse(mon.isWeekend())
        val entries = listOf(DayEntry("h", mon, DayType.HOLIDAY, Instant.EPOCH))
        assertEquals(1, markedWeekdayCount(entries, ym))
    }

    @Test
    fun markedWeekdayCount_includesLeaveOnWeekday() {
        val ym = YearMonth.of(2026, 4)
        val wed = LocalDate.of(2026, 4, 8)
        assertFalse(wed.isWeekend())
        val entries = listOf(DayEntry("l", wed, DayType.LEAVE, Instant.EPOCH))
        assertEquals(1, markedWeekdayCount(entries, ym))
    }
}

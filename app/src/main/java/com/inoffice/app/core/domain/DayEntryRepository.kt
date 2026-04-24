package com.inoffice.app.core.domain

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * Local cache / source of truth for day marks. A future remote implementation can
 * mirror the same operations against your API backed by MongoDB.
 *
 * **Future MongoDB**: collection `day_entries` with documents
 * `{ _id, userId, localDate, type, updatedAt }` and a unique compound index on
 * `(userId, localDate)` for upserts and reporting.
 */
interface DayEntryRepository {
    fun observeEntries(yearMonth: YearMonth): Flow<List<DayEntry>>

    suspend fun getDay(localDate: LocalDate): DayType?

    suspend fun setDayType(localDate: LocalDate, type: DayType)
}

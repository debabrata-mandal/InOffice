package com.inoffice.app.core.domain

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model aligned with a future Mongo document in `day_entries`.
 *
 * @property id Client-generated UUID; maps to Mongo `_id` (string) or a dedicated field.
 * @property localDate Calendar day in the user's zone, stored as `YYYY-MM-DD` for stable indexing.
 * @property type Classification for that date.
 * @property updatedAt Used for last-write-wins sync against the server.
 */
data class DayEntry(
    val id: String,
    val localDate: LocalDate,
    val type: DayType,
    val updatedAt: Instant,
)

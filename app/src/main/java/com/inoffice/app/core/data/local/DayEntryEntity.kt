package com.inoffice.app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a marked calendar day. Field names mirror a practical Mongo mapping:
 * - [localDate] `YYYY-MM-DD` string (indexed, unique)
 * - [id] UUID string suitable as `_id` when replicated
 * - [type] same string values as [com.inoffice.app.core.domain.DayType] name
 * - [updatedAtEpochMillis] maps to BSON Date or ISO string at the API boundary
 */
@Entity(tableName = "day_entries")
data class DayEntryEntity(
    @PrimaryKey val localDate: String,
    val id: String,
    val type: String,
    val updatedAtEpochMillis: Long,
)

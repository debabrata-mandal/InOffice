package com.inoffice.app.core.data.local

import com.inoffice.app.core.domain.DayEntry
import com.inoffice.app.core.domain.DayType
import java.time.Instant
import java.time.LocalDate

internal fun DayEntryEntity.toDomain(): DayEntry =
    DayEntry(
        id = id,
        localDate = LocalDate.parse(localDate),
        type = DayType.valueOf(type),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )

package com.inoffice.app.core.domain

/**
 * How a calendar day is classified for office tracking.
 * Persist this name in Mongo as a string (e.g. `"OFFICE"`).
 */
enum class DayType {
    OFFICE,
    LEAVE,
    HOLIDAY,
    NONE,
}

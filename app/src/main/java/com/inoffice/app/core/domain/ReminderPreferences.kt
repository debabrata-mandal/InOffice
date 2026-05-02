package com.inoffice.app.core.domain

import kotlinx.coroutines.flow.Flow

/** Weekday mark reminder: local time and on/off. */
interface ReminderPreferences {
    fun observeReminderEnabled(): Flow<Boolean>

    fun observeReminderHour(): Flow<Int>

    fun observeReminderMinute(): Flow<Int>

    suspend fun setReminderEnabled(enabled: Boolean)

    suspend fun setReminderTime(hour: Int, minute: Int)

    suspend fun getReminderEnabled(): Boolean

    suspend fun getReminderHour(): Int

    suspend fun getReminderMinute(): Int
}

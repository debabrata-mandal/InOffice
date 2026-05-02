package com.inoffice.app.di

import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.ReminderPreferences
import com.inoffice.app.core.reminder.MarkReminderScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MarkReminderEntryPoint {
    fun dayEntryRepository(): DayEntryRepository

    fun reminderPreferences(): ReminderPreferences

    fun markReminderScheduler(): MarkReminderScheduler
}

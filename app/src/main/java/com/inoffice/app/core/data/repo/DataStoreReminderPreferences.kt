package com.inoffice.app.core.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.inoffice.app.core.domain.ReminderPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(name = "reminder_prefs")

private val KEY_ENABLED = booleanPreferencesKey("reminder_enabled")
private val KEY_HOUR = intPreferencesKey("reminder_hour")
private val KEY_MINUTE = intPreferencesKey("reminder_minute")

@Singleton
class DataStoreReminderPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderPreferences {

    override fun observeReminderEnabled(): Flow<Boolean> =
        context.reminderDataStore.data.map { prefs -> prefs[KEY_ENABLED] ?: false }

    override fun observeReminderHour(): Flow<Int> =
        context.reminderDataStore.data.map { prefs -> prefs[KEY_HOUR] ?: DEFAULT_HOUR }

    override fun observeReminderMinute(): Flow<Int> =
        context.reminderDataStore.data.map { prefs -> prefs[KEY_MINUTE] ?: DEFAULT_MINUTE }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { prefs -> prefs[KEY_ENABLED] = enabled }
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[KEY_HOUR] = hour.coerceIn(0, 23)
            prefs[KEY_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    override suspend fun getReminderEnabled(): Boolean = observeReminderEnabled().first()

    override suspend fun getReminderHour(): Int = observeReminderHour().first()

    override suspend fun getReminderMinute(): Int = observeReminderMinute().first()

    companion object {
        const val DEFAULT_HOUR = 17
        const val DEFAULT_MINUTE = 0
    }
}

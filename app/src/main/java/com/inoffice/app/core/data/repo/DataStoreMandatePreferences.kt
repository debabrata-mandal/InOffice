package com.inoffice.app.core.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.inoffice.app.core.domain.MandatePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mandateDataStore: DataStore<Preferences> by preferencesDataStore(name = "mandate_prefs")

private val KEY_BASE_MANDATE = intPreferencesKey("base_mandate")

@Singleton
class DataStoreMandatePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : MandatePreferences {

    override fun observeBaseMandate(): Flow<Int> =
        context.mandateDataStore.data.map { prefs ->
            prefs[KEY_BASE_MANDATE] ?: DEFAULT_BASE_MANDATE
        }

    override suspend fun setBaseMandate(value: Int) {
        context.mandateDataStore.edit { prefs ->
            prefs[KEY_BASE_MANDATE] = value.coerceAtLeast(0)
        }
    }

    companion object {
        const val DEFAULT_BASE_MANDATE = 12
    }
}

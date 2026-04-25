package com.inoffice.app.core.sync

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.syncMetaDataStore by preferencesDataStore(name = "sync_meta_store")

@Singleton
class SyncDeletionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun getAllDeletedEntries(): List<SyncDeletedEntry> = readMarkersMap().entries
        .map { (localDate, updatedAt) ->
            SyncDeletedEntry(localDate = localDate, updatedAtEpochMillis = updatedAt)
        }
        .sortedBy { it.localDate }

    suspend fun markDeleted(
        localDate: String,
        updatedAtEpochMillis: Long,
    ) {
        val current = readMarkersMap().toMutableMap()
        current[localDate] = updatedAtEpochMillis
        writeMarkersMap(current)
    }

    suspend fun clearDeleted(localDate: String) {
        val current = readMarkersMap().toMutableMap()
        current.remove(localDate)
        writeMarkersMap(current)
    }

    suspend fun replaceAllDeletedEntries(entries: List<SyncDeletedEntry>) {
        val map = entries.associate { it.localDate to it.updatedAtEpochMillis }
        writeMarkersMap(map)
    }

    suspend fun pruneOlderThan(cutoffEpochMillis: Long): Int {
        val current = readMarkersMap()
        if (current.isEmpty()) return 0
        val filtered = current.filterValues { updatedAt -> updatedAt >= cutoffEpochMillis }
        if (filtered.size == current.size) return 0
        writeMarkersMap(filtered)
        return current.size - filtered.size
    }

    private suspend fun readMarkersMap(): Map<String, Long> {
        val raw =
            context.syncMetaDataStore.data
                .map { prefs -> prefs[KEY_DELETED_MARKERS_JSON].orEmpty() }
                .first()
        if (raw.isBlank()) return emptyMap()
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyMap()
        return buildMap {
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.optLong(key, Long.MIN_VALUE)
                if (value != Long.MIN_VALUE) {
                    put(key, value)
                }
            }
        }
    }

    private suspend fun writeMarkersMap(markers: Map<String, Long>) {
        val json = JSONObject().apply { markers.forEach { (k, v) -> put(k, v) } }.toString()
        context.syncMetaDataStore.edit { prefs: MutablePreferences ->
            prefs[KEY_DELETED_MARKERS_JSON] = json
        }
    }

    companion object {
        private val KEY_DELETED_MARKERS_JSON = stringPreferencesKey("deleted_markers_json")
    }
}

package com.inoffice.app.core.sync

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncSnapshotJsonCodec @Inject constructor() {
    fun encode(snapshot: SyncSnapshot): String {
        val root =
            JSONObject()
                .put("schemaVersion", snapshot.schemaVersion)
                .put("snapshotUpdatedAtEpochMillis", snapshot.snapshotUpdatedAtEpochMillis)
                .put("baseMandate", snapshot.baseMandate)
                .put("baseMandateUpdatedAtEpochMillis", snapshot.baseMandateUpdatedAtEpochMillis)
                .put(
                    "dayEntries",
                    JSONArray().apply {
                        snapshot.dayEntries.forEach { entry ->
                            put(
                                JSONObject()
                                    .put("id", entry.id)
                                    .put("localDate", entry.localDate)
                                    .put("type", entry.type)
                                    .put("updatedAtEpochMillis", entry.updatedAtEpochMillis),
                            )
                        }
                    },
                )
                .put(
                    "deletedEntries",
                    JSONArray().apply {
                        snapshot.deletedEntries.forEach { entry ->
                            put(
                                JSONObject()
                                    .put("localDate", entry.localDate)
                                    .put("updatedAtEpochMillis", entry.updatedAtEpochMillis),
                            )
                        }
                    },
                )
        return root.toString()
    }

    fun decode(rawJson: String): SyncSnapshot {
        val root = JSONObject(rawJson)
        val entriesArray = root.optJSONArray("dayEntries") ?: JSONArray()
        val deletedArray = root.optJSONArray("deletedEntries") ?: JSONArray()
        val entries =
            buildList(entriesArray.length()) {
                for (index in 0 until entriesArray.length()) {
                    val obj = entriesArray.getJSONObject(index)
                    add(
                        SyncDayEntry(
                            id = obj.getString("id"),
                            localDate = obj.getString("localDate"),
                            type = obj.getString("type"),
                            updatedAtEpochMillis = obj.getLong("updatedAtEpochMillis"),
                        ),
                    )
                }
            }
        val deletedEntries =
            buildList(deletedArray.length()) {
                for (index in 0 until deletedArray.length()) {
                    val obj = deletedArray.getJSONObject(index)
                    add(
                        SyncDeletedEntry(
                            localDate = obj.getString("localDate"),
                            updatedAtEpochMillis = obj.getLong("updatedAtEpochMillis"),
                        ),
                    )
                }
            }
        return SyncSnapshot(
            schemaVersion = root.optInt("schemaVersion", 1),
            snapshotUpdatedAtEpochMillis = root.optLong("snapshotUpdatedAtEpochMillis", 0L),
            baseMandate = root.optInt("baseMandate", 12),
            baseMandateUpdatedAtEpochMillis = root.optLong("baseMandateUpdatedAtEpochMillis", 0L),
            dayEntries = entries,
            deletedEntries = deletedEntries,
        )
    }
}

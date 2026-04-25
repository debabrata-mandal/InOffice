package com.inoffice.app.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncMergeEngineTest {
    private val engine = SyncMergeEngine()

    @Test
    fun `latest update wins for same date`() {
        val local =
            snapshot(
                dayEntries =
                    listOf(
                        SyncDayEntry(
                            id = "a",
                            localDate = "2026-04-20",
                            type = "OFFICE",
                            updatedAtEpochMillis = 100,
                        ),
                    ),
            )
        val remote =
            snapshot(
                dayEntries =
                    listOf(
                        SyncDayEntry(
                            id = "a",
                            localDate = "2026-04-20",
                            type = "LEAVE",
                            updatedAtEpochMillis = 200,
                        ),
                    ),
            )

        val merged = engine.merge(local, remote)

        assertEquals(1, merged.dayEntries.size)
        assertEquals("LEAVE", merged.dayEntries.first().type)
        assertTrue(merged.deletedEntries.isEmpty())
    }

    @Test
    fun `newer delete beats older update`() {
        val local =
            snapshot(
                dayEntries =
                    listOf(
                        SyncDayEntry(
                            id = "a",
                            localDate = "2026-04-20",
                            type = "OFFICE",
                            updatedAtEpochMillis = 100,
                        ),
                    ),
            )
        val remote =
            snapshot(
                deletedEntries =
                    listOf(
                        SyncDeletedEntry(
                            localDate = "2026-04-20",
                            updatedAtEpochMillis = 200,
                        ),
                    ),
            )

        val merged = engine.merge(local, remote)

        assertTrue(merged.dayEntries.isEmpty())
        assertEquals(1, merged.deletedEntries.size)
        assertEquals("2026-04-20", merged.deletedEntries.first().localDate)
    }

    @Test
    fun `newer update beats older delete`() {
        val local =
            snapshot(
                deletedEntries =
                    listOf(
                        SyncDeletedEntry(
                            localDate = "2026-04-20",
                            updatedAtEpochMillis = 100,
                        ),
                    ),
            )
        val remote =
            snapshot(
                dayEntries =
                    listOf(
                        SyncDayEntry(
                            id = "a",
                            localDate = "2026-04-20",
                            type = "HOLIDAY",
                            updatedAtEpochMillis = 200,
                        ),
                    ),
            )

        val merged = engine.merge(local, remote)

        assertEquals(1, merged.dayEntries.size)
        assertEquals("HOLIDAY", merged.dayEntries.first().type)
        assertTrue(merged.deletedEntries.isEmpty())
    }

    private fun snapshot(
        dayEntries: List<SyncDayEntry> = emptyList(),
        deletedEntries: List<SyncDeletedEntry> = emptyList(),
    ): SyncSnapshot =
        SyncSnapshot(
            schemaVersion = 1,
            snapshotUpdatedAtEpochMillis = 0,
            baseMandate = 12,
            baseMandateUpdatedAtEpochMillis = 0,
            dayEntries = dayEntries,
            deletedEntries = deletedEntries,
        )
}

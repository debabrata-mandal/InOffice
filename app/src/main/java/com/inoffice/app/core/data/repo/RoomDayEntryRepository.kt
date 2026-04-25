package com.inoffice.app.core.data.repo

import com.inoffice.app.core.data.local.DayEntryDao
import com.inoffice.app.core.data.local.DayEntryEntity
import com.inoffice.app.core.data.local.toDomain
import com.inoffice.app.core.domain.DayEntry
import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.DayType
import com.inoffice.app.core.sync.SyncDeletionStore
import com.inoffice.app.core.sync.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDayEntryRepository @Inject constructor(
    private val dao: DayEntryDao,
    private val deletionStore: SyncDeletionStore,
    private val syncTrigger: SyncTrigger,
) : DayEntryRepository {

    override fun observeEntries(yearMonth: YearMonth): Flow<List<DayEntry>> {
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()
        return dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDay(localDate: LocalDate): DayType? {
        val row = dao.getByDate(localDate.toString()) ?: return null
        return DayType.valueOf(row.type)
    }

    override suspend fun setDayType(localDate: LocalDate, type: DayType) {
        val key = localDate.toString()
        val now = System.currentTimeMillis()
        if (type == DayType.NONE) {
            dao.deleteByDate(key)
            deletionStore.markDeleted(localDate = key, updatedAtEpochMillis = now)
            syncTrigger.onLocalDataChanged()
            return
        }
        deletionStore.clearDeleted(key)
        val existing = dao.getByDate(key)
        val id = existing?.id ?: UUID.randomUUID().toString()
        dao.upsert(
            DayEntryEntity(
                localDate = key,
                id = id,
                type = type.name,
                updatedAtEpochMillis = now,
            ),
        )
        syncTrigger.onLocalDataChanged()
    }
}

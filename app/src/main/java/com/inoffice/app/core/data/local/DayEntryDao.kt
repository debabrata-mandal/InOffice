package com.inoffice.app.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEntryDao {
    @Query(
        "SELECT * FROM day_entries WHERE localDate >= :fromInclusive AND localDate <= :toInclusive ORDER BY localDate ASC",
    )
    fun observeBetween(fromInclusive: String, toInclusive: String): Flow<List<DayEntryEntity>>

    @Query("SELECT * FROM day_entries WHERE localDate = :localDate LIMIT 1")
    suspend fun getByDate(localDate: String): DayEntryEntity?

    @Upsert
    suspend fun upsert(entity: DayEntryEntity)

    @Query("DELETE FROM day_entries WHERE localDate = :localDate")
    suspend fun deleteByDate(localDate: String)
}

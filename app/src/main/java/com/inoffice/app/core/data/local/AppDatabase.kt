package com.inoffice.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DayEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dayEntryDao(): DayEntryDao
}

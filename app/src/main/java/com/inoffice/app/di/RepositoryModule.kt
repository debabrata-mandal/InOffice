package com.inoffice.app.di

import com.inoffice.app.core.data.repo.DataStoreMandatePreferences
import com.inoffice.app.core.data.repo.RoomDayEntryRepository
import com.inoffice.app.core.domain.DayEntryRepository
import com.inoffice.app.core.domain.MandatePreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDayEntryRepository(impl: RoomDayEntryRepository): DayEntryRepository

    @Binds
    @Singleton
    abstract fun bindMandatePreferences(impl: DataStoreMandatePreferences): MandatePreferences
}

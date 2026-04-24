package com.inoffice.app.core.domain

import kotlinx.coroutines.flow.Flow

/**
 * Device-local mandate configuration. When you add MongoDB, consider a `user_settings`
 * document (e.g. `{ userId, baseMandate, updatedAt }`) and sync it alongside [DayEntryRepository].
 */
interface MandatePreferences {
    fun observeBaseMandate(): Flow<Int>

    suspend fun setBaseMandate(value: Int)
}

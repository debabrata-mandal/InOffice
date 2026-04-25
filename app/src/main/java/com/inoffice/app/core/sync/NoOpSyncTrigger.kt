package com.inoffice.app.core.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSyncTrigger @Inject constructor() : SyncTrigger {
    override fun onLocalDataChanged() = Unit
}

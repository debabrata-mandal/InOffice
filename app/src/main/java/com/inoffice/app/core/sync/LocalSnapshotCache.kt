package com.inoffice.app.core.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSnapshotCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val codec: SyncSnapshotJsonCodec,
) {
    private val snapshotFile: File by lazy { File(context.filesDir, FILE_NAME) }

    fun read(): SyncSnapshot? {
        if (!snapshotFile.exists()) {
            return null
        }
        return runCatching { codec.decode(snapshotFile.readText()) }.getOrNull()
    }

    fun write(snapshot: SyncSnapshot) {
        snapshotFile.writeText(codec.encode(snapshot))
    }

    companion object {
        private const val FILE_NAME = "inoffice_sync_cache.json"
    }
}

package com.inoffice.app

import android.app.Application
import com.inoffice.app.core.reminder.MarkReminderNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InOfficeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MarkReminderNotifications.ensureChannel(this)
    }
}

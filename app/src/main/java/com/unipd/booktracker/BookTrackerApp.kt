package com.unipd.booktracker

import android.app.Application
import com.google.android.material.color.DynamicColors

class BookTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
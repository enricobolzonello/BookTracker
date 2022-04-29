package com.unipd.booktracker

import android.app.ActivityOptions
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class BookTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
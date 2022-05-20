package com.unipd.booktracker

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors

class BookTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs.getBoolean(getString(R.string.dynamic_colors_key), true))
            DynamicColors.applyToActivitiesIfAvailable(this)

        prefs.getString(getString(R.string.app_theme_key), getString(R.string.system))?.let {
            BookTrackerUtils.setModeNight(this, it)
        }
    }
}
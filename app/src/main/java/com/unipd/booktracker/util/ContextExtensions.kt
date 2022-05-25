package com.unipd.booktracker.util

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import com.unipd.booktracker.R

const val LARGE_SCREEN_SW = 600

fun Context.getAttrId(attrId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue, true)
    return typedValue.resourceId
}

fun Context.setModeNight(value: String) {
    val mode = when (value) {
        getString(R.string.light) -> AppCompatDelegate.MODE_NIGHT_NO
        getString(R.string.dark) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

fun Context.isLargeScreen(): Boolean {
    return resources.configuration.smallestScreenWidthDp >= LARGE_SCREEN_SW
}

fun Context.isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.isSideBySideMode(): Boolean {
    return this.isLargeScreen() && this.isLandscape()
}
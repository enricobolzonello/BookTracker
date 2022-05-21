package com.unipd.booktracker.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.use
import com.unipd.booktracker.R

// Retrieve a color from the current theme
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun Context.setModeNight(value: String) {
    val mode = when(value) {
        getString(R.string.light) -> AppCompatDelegate.MODE_NIGHT_NO
        getString(R.string.dark) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

fun Context.isLargeScreen(): Boolean {
    return resources.configuration.smallestScreenWidthDp >= 600
}
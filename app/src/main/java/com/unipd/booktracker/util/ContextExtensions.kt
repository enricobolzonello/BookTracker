package com.unipd.booktracker.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import com.unipd.booktracker.R

fun Context.getAttrId(attrId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue,true)
    return typedValue.resourceId
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
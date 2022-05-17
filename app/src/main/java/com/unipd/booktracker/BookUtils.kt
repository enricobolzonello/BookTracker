package com.unipd.booktracker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import java.io.ByteArrayOutputStream

class BookUtils {
    companion object {
        fun fromBitmap(bmp: Bitmap?): ByteArray? {
            if (bmp == null)
                return null
            val outputStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return outputStream.toByteArray()
        }

        fun toBitmap(bytes: ByteArray?): Bitmap? {
            if (bytes == null)
                return null
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        fun getColor(context: Context, colorResId: Int): Int {
            val typedValue = TypedValue()
            val typedArray: TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
            val color = typedArray.getColor(0, 0)
            typedArray.recycle()
            return color
        }

        fun setModeNight(context: Context, value: String) {
            val mode = when(value) {
                context.getString(R.string.light) -> AppCompatDelegate.MODE_NIGHT_NO
                context.getString(R.string.dark) -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        fun isLargeScreen(context: Context): Boolean {
            return context.resources.configuration.smallestScreenWidthDp >= 600
        }
    }
}
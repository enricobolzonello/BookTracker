package com.unipd.booktracker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.util.Date

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
    }
}
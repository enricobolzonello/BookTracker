package com.unipd.booktracker.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

fun Bitmap?.toByteArray(): ByteArray? {
    if (this == null)
        return null
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()
}

fun ByteArray?.toBitMap(): Bitmap? {
    if (this == null)
        return null
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}
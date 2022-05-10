package com.unipd.booktracker.db

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.io.Serializable

enum class OrderColumns {
    title, author, year, progress
}

@Entity(tableName = "books", primaryKeys = ["id"])
class Book(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "mainAuthor") val mainAuthor: String,
    @ColumnInfo(name = "pages") val pages: Int,
    @Nullable@ColumnInfo(name = "publisher") val publisher: String?,
    @Nullable@ColumnInfo(name = "isbn") val isbn: String?,
    @Nullable@ColumnInfo(name = "mainCategory") val mainCategory: String?,
    @Nullable@ColumnInfo(name = "description") val description: String?,
    @Nullable@ColumnInfo(name = "year") val year: Int?,
    @Nullable@ColumnInfo(name = "language") val language: String?,
    @Nullable@ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB) val thumbnail: ByteArray?,
    @Nullable@ColumnInfo(name = "readPages") val readPages: Int? = null
): Serializable

@Entity(
    tableName = "readings",
    primaryKeys = ["bookId", "date"],
    foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("bookId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
class Reading(
    @ColumnInfo(name = "bookId", index = true) val bookId: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "pageDifference") val pageDifference: Int
): Serializable
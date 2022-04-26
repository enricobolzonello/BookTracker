package com.unipd.booktracker.db

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

enum class OrderColumns {
    title, author
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey@ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "pages") val pages: Int,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "publisher") val publisher: String,
    @ColumnInfo(name = "isbn") val isbn: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "publishedDate") val publishedDate: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String,
    @Nullable@ColumnInfo(name = "readPages") val readPages: Int? = null
)

@Entity(
    tableName = "readings",
    foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("bookId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "bookId", index = true) val bookId: String,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "pageDifference") val pageDifference: Int
)
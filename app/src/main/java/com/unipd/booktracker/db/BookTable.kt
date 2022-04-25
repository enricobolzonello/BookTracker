package com.unipd.booktracker.db

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_table")
open class Book(
    @PrimaryKey@ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "pages") val pages: Int,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "publisher") val publisher: String,
    @ColumnInfo(name = "isbn") val isbn: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String,
    @Nullable@ColumnInfo(name = "readPages") val readPages: Int? = null
)
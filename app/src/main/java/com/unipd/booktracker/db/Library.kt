package com.unipd.booktracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library")
class LibraryBook(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "authors") val authors: List<String>,
    @ColumnInfo(name = "pages") val pages: Int,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String
)
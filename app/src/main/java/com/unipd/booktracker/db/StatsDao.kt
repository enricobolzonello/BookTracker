package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface StatsDao {

    @Query("SELECT COUNT(*) FROM books WHERE readPages = pages")
    fun countReadBooks(): Int

    @Query("SELECT SUM(readPages) FROM books")
    fun countReadPages(): Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE date = :date")
    fun countReadPages(date: String): Int

    @Query("SELECT AVG(pageDifference) FROM readings GROUP BY strftime('%Y-%m-%d', date)")
    fun avgReadPagesByDay(): Int

    @Query("SELECT mainAuthor FROM books GROUP BY mainAuthor ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadAuthor(): String

    @Query("SELECT COUNT(DISTINCT mainAuthor) FROM books")
    fun countAuthors(): Int

    @Query("SELECT mainCategory FROM books GROUP BY mainCategory ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadCategory(): String

    @Query("SELECT COUNT(DISTINCT mainCategory) FROM books")
    fun countCategories(): Int

}
package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface StatsDao {

    @Query("SELECT SUM(readPages) FROM books")
    fun countReadPages(): Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE date = :day")
    fun countReadPages(day: String): Int

    @Query("SELECT AVG(pageDifference) FROM readings WHERE date < :day GROUP BY strftime('%Y-%m-%d', date)")
    fun avgReadPagesByDay(day: String): Int

    @Query("SELECT COUNT(*) FROM books WHERE readPages = pages")
    fun countReadBooks(): Int

    @Query("SELECT COUNT(*) FROM books WHERE readPages = pages " +
            "AND :year = " +
                "(SELECT strftime('%Y', date) FROM readings WHERE books.id = bookId ORDER BY date DESC LIMIT 1)")
    fun countReadBooks(year: String): Int

    @Query("SELECT AVG(readBooks) FROM " +
                    "(SELECT COUNT(*) AS readBooks FROM books WHERE readPages = pages " +
                    "GROUP BY " +
                        "(SELECT strftime('%Y', date) AS year FROM readings WHERE books.id = bookId AND year < :year ORDER BY date DESC LIMIT 1))")
    fun avgReadBooksByYear(year: String): Int

    @Query("SELECT mainAuthor FROM books GROUP BY mainAuthor ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadAuthor(): String

    @Query("SELECT COUNT(DISTINCT mainAuthor) FROM books")
    fun countAuthors(): Int

    @Query("SELECT mainCategory FROM books GROUP BY mainCategory ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadCategory(): String

    @Query("SELECT COUNT(DISTINCT mainCategory) FROM books")
    fun countCategories(): Int

}
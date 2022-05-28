package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface StatsDao {

    @Query("SELECT SUM(readPages) FROM books")
    fun countReadPages(): Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE date = :day AND pageDifference > 0")
    fun countReadPages(day: String): Int

    @Query("SELECT AVG(pageDifference) FROM readings WHERE date < :day AND pageDifference > 0 GROUP BY strftime('%Y-%m-%d', date)")
    fun avgReadPagesByDay(day: String): Int

    @Query("SELECT COUNT(*) FROM books WHERE readPages = pages")
    fun countReadBooks(): Int

    @Query(
        "SELECT COUNT(*) FROM books WHERE readPages = pages AND :year = " +
                "(SELECT strftime('%Y', date) FROM readings WHERE books.id = bookId ORDER BY date DESC LIMIT 1)"
    )
    fun countReadBooks(year: String): Int

    @Query(
        "SELECT AVG(readBooks) FROM (" +
                "SELECT COUNT(*) AS readBooks FROM books WHERE readPages = pages " +
                "GROUP BY " +
                "(SELECT strftime('%Y', date) AS year FROM readings WHERE books.id = bookId ORDER BY date DESC LIMIT 1) " +
                "HAVING " +
                "(SELECT strftime('%Y', date) AS year FROM readings WHERE books.id = bookId ORDER BY date DESC LIMIT 1) " +
                "< :year " +
                ")"
    )
    fun avgReadBooksByYear(year: String): Int

    @Query("SELECT mainAuthor FROM books WHERE readPages IS NOT NULL GROUP BY mainAuthor ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadAuthor(): String?

    @Query("SELECT COUNT(DISTINCT mainAuthor) FROM books WHERE readPages IS NOT NULL")
    fun countAuthors(): Int

    @Query("SELECT mainCategory FROM books  WHERE readPages IS NOT NULL GROUP BY mainCategory ORDER BY COUNT(*) DESC LIMIT 1")
    fun mostReadCategory(): String?

    @Query("SELECT COUNT(DISTINCT mainCategory) FROM books WHERE readPages IS NOT NULL")
    fun countCategories(): Int

}
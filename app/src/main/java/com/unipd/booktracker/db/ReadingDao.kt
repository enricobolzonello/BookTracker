package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: Reading)

    @Query("SELECT SUM(pageDifference) FROM reading_table")
    fun countReadPages() : Int

    @Query("SELECT SUM(pageDifference) FROM reading_table WHERE date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(firstDate: Long, lastDate: Long) : Int

    @Query("SELECT SUM(pageDifference) FROM reading_table WHERE bookId = :book_id")
    fun countReadPages(book_id: String) : Int

    @Query("SELECT SUM(pageDifference) FROM reading_table WHERE bookId = :book_id AND date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(book_id: String, firstDate: Long, lastDate: Long) : Int

    @Query("SELECT AVG(pageDifference) FROM reading_table GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch')")
    fun avgReadPagesByDay() : Int

    @Query("SELECT AVG(pageDifference) FROM reading_table GROUP BY strftime('%Y-%m', date / 1000, 'unixepoch')")
    fun avgReadPagesByMonth() : Int

    @Query("DELETE FROM reading_table where bookId = :book_id")
    fun deleteBookReadings(book_id: String) : Int

    @Query("DELETE FROM reading_table")
    fun deleteReadings()

}
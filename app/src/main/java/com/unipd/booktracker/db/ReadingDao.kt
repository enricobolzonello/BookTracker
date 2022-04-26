package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reading: Reading)

    @Query("SELECT SUM(pageDifference) FROM readings")
    fun countReadPages() : Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(firstDate: Long, lastDate: Long) : Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE bookId = :bookId")
    fun countReadPages(bookId: String) : Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE bookId = :bookId AND date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(bookId: String, firstDate: Long, lastDate: Long) : Int

    @Query("SELECT AVG(pageDifference) FROM readings GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch')")
    fun avgReadPagesByDay() : Int

    @Query("SELECT AVG(pageDifference) FROM readings GROUP BY strftime('%Y-%m', date / 1000, 'unixepoch')")
    fun avgReadPagesByMonth() : Int

    @Query("DELETE FROM readings")
    fun deleteReadings()

}
package com.unipd.booktracker.db

import androidx.room.*
import java.util.*


@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(reading: Reading): Long

    @Query("UPDATE readings " +
            "SET pageDifference = pageDifference + :pageDifference " +
            "WHERE bookId = :bookId AND date = :date")
    fun updatePages(bookId: String, date: String, pageDifference: Int)

    @Transaction
    fun upsert(reading: Reading) {
        // If a reading record already exists, then update the pageDifference column
        if (insert(reading) == -1L)
            updatePages(reading.bookId, reading.date, reading.pageDifference)
    }

    @Query("DELETE FROM readings WHERE bookId = :bookId")
    fun deleteBookReadings(bookId: String)

    @Query("DELETE FROM readings")
    fun deleteAllReadings()

    @Query("SELECT SUM(pageDifference) FROM readings")
    fun countReadPages() : Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(firstDate: Date, lastDate: Date) : Int

    @Query("SELECT SUM(pageDifference) FROM readings WHERE bookId = :bookId AND date BETWEEN :firstDate AND :lastDate")
    fun countReadPages(bookId: String, firstDate: Date, lastDate: Date) : Int

    @Query("SELECT AVG(pageDifference) FROM readings GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch')")
    fun avgReadPagesByDay() : Int

    @Query("SELECT AVG(pageDifference) FROM readings GROUP BY strftime('%Y-%m', date / 1000, 'unixepoch')")
    fun avgReadPagesByMonth() : Int

}
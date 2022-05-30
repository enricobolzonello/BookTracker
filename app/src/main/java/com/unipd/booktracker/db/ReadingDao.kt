package com.unipd.booktracker.db

import androidx.room.*

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(reading: Reading): Long

    @Query(
        "UPDATE readings " +
                "SET pageDifference = pageDifference + :pageDifference " +
                "WHERE bookId = :bookId AND date = :date"
    )
    fun updatePages(bookId: String, date: String, pageDifference: Int)

    @Transaction
    fun upsert(reading: Reading) {
        // If a reading record for the same (book,date) pair already exists, then update the pageDifference column
        if (insert(reading) == -1L)
            updatePages(reading.bookId, reading.date, reading.pageDifference)
    }

    @Query("DELETE FROM readings WHERE bookId =:bookId")
    fun deleteBookReadings(bookId: String)

    @Query("SELECT * FROM readings")
    fun getReadings(): List<Reading>
}
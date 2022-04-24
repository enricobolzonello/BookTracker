package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library")
    fun getObservableBooks(): LiveData<List<LibraryBook>>

    @Query("SELECT * FROM library ORDER BY title ASC")
    fun getBooksByTitle(): List<LibraryBook>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: LibraryBook)

    @Delete
    suspend fun delete(book: LibraryBook)

    @Query("DELETE FROM library")
    fun deleteAllBooks()

    @Query("SELECT COUNT(*) FROM library")
    fun countBooks(): Int
}
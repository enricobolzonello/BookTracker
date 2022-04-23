package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library ORDER BY title ASC")
    fun getBooksByTitle(): LiveData<List<LibraryBook>>

    @Insert
    suspend fun insert(book: LibraryBook)

    @Delete
    suspend fun delete(book: LibraryBook)

}
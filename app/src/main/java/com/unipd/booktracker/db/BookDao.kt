package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Query("SELECT * FROM book_table WHERE readPages IS NOT NULL")
    fun getObservableLibrary(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE readPages IS NULL")
    fun getObservableWishlist(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE readPages IS NOT NULL ORDER BY title ASC")
    fun getLibraryByTitle(): List<Book>

    @Query("SELECT * FROM book_table WHERE readPages IS NULL ORDER BY title ASC")
    fun getWishlistByTitle(): List<Book>

    @Query("SELECT * FROM book_table WHERE readPages = 0")
    fun getNotReadBooks(): List<Book>

    @Query("SELECT * FROM book_table WHERE readPages > 0 AND readPages < pages")
    fun getReadingBooks(): List<Book>

    @Query("SELECT * FROM book_table WHERE readPages = pages")
    fun getReadBooks(): List<Book>

    @Query("SELECT COUNT(*) FROM book_table WHERE readPages IS NOT NULL")
    fun countLibraryBooks(): Int

    @Query("SELECT COUNT(*) FROM book_table WHERE readPages IS NULL")
    fun countWishlistBooks(): Int

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM book_table WHERE readPages IS NOT NULL")
    fun deleteLibraryBooks()

    @Query("DELETE FROM book_table WHERE readPages IS NULL")
    fun deleteWishlistBooks()
}
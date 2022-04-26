package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: Book)

    @Query("SELECT * FROM books WHERE readPages IS NOT NULL")
    fun getObservableLibrary(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE readPages IS NULL")
    fun getObservableWishlist(): LiveData<List<Book>>

    @Query("SELECT * FROM " +
            "(SELECT * FROM books WHERE :notRead AND readPages = 0 " +
            "UNION SELECT * FROM books WHERE :reading AND (readPages > 0 AND readPages < pages) " +
            "UNION SELECT * FROM books WHERE :read AND readPages = pages)" +
            "ORDER BY " +
            "CASE WHEN :asc THEN " +
                "CASE WHEN :orderColumn = 'title' THEN title " +
                "WHEN :orderColumn = 'author' THEN author END " +
            "END ASC, " +
            "CASE WHEN NOT :asc THEN " +
                "CASE WHEN :orderColumn = 'title' THEN title " +
                "WHEN :orderColumn = 'author' THEN author END " +
            "END DESC")
    fun getFilteredLibrary(notRead: Boolean, reading: Boolean, read: Boolean, orderColumn: OrderColumns, asc: Boolean): List<Book>

    @Query("SELECT * FROM books WHERE readPages IS NULL " +
            "ORDER BY " +
            "CASE WHEN :asc THEN " +
                "CASE WHEN :orderColumn = 'title' THEN title " +
                "WHEN :orderColumn = 'author' THEN author END " +
            "END ASC, " +
            "CASE WHEN NOT :asc THEN " +
                "CASE WHEN :orderColumn = 'title' THEN title " +
                "WHEN :orderColumn = 'author' THEN author END " +
            "END DESC")
    fun getFilteredWishlist(orderColumn : OrderColumns, asc : Boolean): List<Book>

    @Query("SELECT COUNT(*) FROM books WHERE readPages IS NOT NULL")
    fun countLibraryBooks(): Int

    @Query("SELECT COUNT(*) FROM books WHERE readPages IS NULL")
    fun countWishlistBooks(): Int

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books WHERE readPages IS NOT NULL")
    fun deleteLibraryBooks()

    @Query("DELETE FROM books WHERE readPages IS NULL")
    fun deleteWishlistBooks()

    @Query("DELETE FROM books")
    fun deleteBooks()
}
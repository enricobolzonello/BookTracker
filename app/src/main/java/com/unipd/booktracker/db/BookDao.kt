package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: Book)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(books: List<Book>)

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBook(bookId: String): Book

    @Query ("SELECT EXISTS (SELECT 1 FROM books WHERE id = :bookId)")
    fun findBook(bookId: String): Boolean

    @Query("UPDATE books SET readPages = (SELECT SUM(pageDifference) FROM readings WHERE bookId = :bookId) WHERE books.id = :bookId")
    suspend fun updateReadPages(bookId: String) : Int

    @Query("SELECT * FROM books WHERE readPages IS NOT NULL")
    fun getObservableLibrary(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE readPages IS NULL")
    fun getObservableWishlist(): LiveData<List<Book>>

    @Query("SELECT * FROM " +
            "(SELECT * FROM books WHERE :notRead AND readPages = 0 " +
            "UNION SELECT * FROM books WHERE :reading AND (readPages > 0 AND readPages < pages) " +
            "UNION SELECT * FROM books WHERE :read AND readPages = pages)" +
            "WHERE readPages IS NOT NULL " +
            "AND title LIKE '%' || :query || '%' OR mainAuthor LIKE '%' || :query || '%'" +
            "ORDER BY " +
            "CASE WHEN :asc THEN " +
                "CASE " +
                    "WHEN :orderColumn = 'title' THEN title " +
                    "WHEN :orderColumn = 'author' THEN mainAuthor " +
                    "WHEN :orderColumn = 'year' THEN year " +
                    "WHEN :orderColumn = 'progress' THEN (CAST(readPages AS float) / CAST(pages AS FLOAT)) " +
                "END " +
            "END ASC, " +
            "CASE WHEN NOT :asc THEN " +
                "CASE " +
                    "WHEN :orderColumn = 'title' THEN title " +
                    "WHEN :orderColumn = 'author' THEN mainAuthor " +
                    "WHEN :orderColumn = 'year' THEN year " +
                    "WHEN :orderColumn = 'progress' THEN (CAST(readPages AS float) / CAST(pages AS FLOAT)) " +
                "END " +
            "END DESC")
    fun getFilteredLibrary(query: String, notRead: Boolean, reading: Boolean, read: Boolean, orderColumn: OrderColumns, asc: Boolean): List<Book>

    @Query("SELECT * FROM books " +
            "WHERE readPages IS NULL " +
            "AND title LIKE '%' || :query || '%' OR mainAuthor LIKE '%' || :query || '%'" +
            "ORDER BY " +
            "CASE WHEN :asc THEN " +
                "CASE " +
                    "WHEN :orderColumn = 'title' THEN title " +
                    "WHEN :orderColumn = 'author' THEN mainAuthor " +
                    "WHEN :orderColumn = 'year' THEN year " +
                "END " +
            "END ASC, " +
            "CASE WHEN NOT :asc THEN " +
                "CASE " +
                    "WHEN :orderColumn = 'title' THEN title " +
                    "WHEN :orderColumn = 'author' THEN mainAuthor " +
                    "WHEN :orderColumn = 'year' THEN year " +
                "END " +
            "END DESC")
    fun getFilteredWishlist(query: String, orderColumn : OrderColumns, asc : Boolean): List<Book>

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
package com.unipd.booktracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book)

    @Delete
    fun delete(book: Book)

    @Query("DELETE FROM books")
    fun deleteBooks()

    @Query("SELECT * FROM books")
    fun getBooks(): List<Book>

    @Query("SELECT * FROM books WHERE readPages IS NOT NULL")
    fun getObservableLibrary(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE readPages IS NULL")
    fun getObservableWishlist(): LiveData<List<Book>>

    @Query("SELECT EXISTS (SELECT * FROM books WHERE id = :bookId AND readPages IS NOT NULL)")
    fun isBookInLibrary(bookId: String): Boolean

    @Query("SELECT EXISTS (SELECT * FROM books WHERE id = :bookId AND readPages IS NULL)")
    fun isBookInWishlist(bookId: String): Boolean

    @Query("UPDATE books SET readPages = 0 WHERE id = :bookId")
    fun moveToLibrary(bookId: String)

    @Query("UPDATE books SET readPages = NULL WHERE id = :bookId")
    fun moveToWishlist(bookId: String)

    @Query(
        "SELECT * FROM " +
                "(SELECT * FROM books WHERE :notRead AND readPages = 0 " +
                "UNION SELECT * FROM books WHERE :reading AND (readPages > 0 AND readPages < pages) " +
                "UNION SELECT * FROM books WHERE :read AND readPages = pages)" +
                "WHERE readPages IS NOT NULL " +
                "AND title LIKE '%' || :query || '%' OR mainAuthor LIKE '%' || :query || '%'" +
                "ORDER BY " +
                "CASE WHEN :asc THEN " +
                "CASE " +
                "WHEN :orderColumn = 'Title' THEN title " +
                "WHEN :orderColumn = 'Author' THEN mainAuthor " +
                "WHEN :orderColumn = 'Year' THEN year " +
                "WHEN :orderColumn = 'Progress' THEN (CAST(readPages AS float) / CAST(pages AS FLOAT)) " +
                "END " +
                "END ASC, " +
                "CASE WHEN NOT :asc THEN " +
                "CASE " +
                "WHEN :orderColumn = 'Title' THEN title " +
                "WHEN :orderColumn = 'Author' THEN mainAuthor " +
                "WHEN :orderColumn = 'Year' THEN year " +
                "WHEN :orderColumn = 'Progress' THEN (CAST(readPages AS float) / CAST(pages AS FLOAT)) " +
                "END " +
                "END DESC"
    )
    fun getFilteredLibrary(
        query: String,
        notRead: Boolean,
        reading: Boolean,
        read: Boolean,
        orderColumn: OrderColumn,
        asc: Boolean
    ): List<Book>

    @Query(
        "SELECT * FROM books " +
                "WHERE readPages IS NULL " +
                "AND (title LIKE '%' || :query || '%' OR mainAuthor LIKE '%' || :query || '%') " +
                "ORDER BY " +
                "CASE WHEN :asc THEN " +
                "CASE " +
                "WHEN :orderColumn = 'Title' THEN title " +
                "WHEN :orderColumn = 'Author' THEN mainAuthor " +
                "WHEN :orderColumn = 'Year' THEN year " +
                "END " +
                "END ASC, " +
                "CASE WHEN NOT:asc THEN " +
                "CASE " +
                "WHEN :orderColumn = 'Title' THEN title " +
                "WHEN :orderColumn = 'Author' THEN mainAuthor " +
                "WHEN :orderColumn = 'Year' THEN year " +
                "END " +
                "END DESC"
    )
    fun getFilteredWishlist(query: String, orderColumn: OrderColumn, asc: Boolean): List<Book>
}
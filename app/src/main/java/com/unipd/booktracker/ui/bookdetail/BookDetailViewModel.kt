package com.unipd.booktracker.ui.bookdetail

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
   This ViewModel intermediates between BookDetailFragment and Room Database
   Database action need to be performed in the IO thread instead of the Main one
*/
class BookDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDatabase: BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao: BookDao = bookDatabase.bookDao()
    private val readingDao: ReadingDao = bookDatabase.readingDao()

    fun addBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.insert(book)
    }

    fun addReadPages(book: Book, pageDifference: Int) = viewModelScope.launch(Dispatchers.IO) {
        readingDao.upsert(Reading(book.id, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), pageDifference))
    }

    fun isBookInLibrary(book: Book): Boolean = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.isBookInLibrary(book.id)
    }

    fun isBookInWishlist(book: Book): Boolean = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.isBookInWishlist(book.id)
    }

    fun moveToLibrary(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.moveToLibrary(book.id)
    }

    fun moveToWishlist(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.moveToWishlist(book.id)
        readingDao.deleteBookReadings(book.id)
    }

    fun removeBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.delete(book)
    }
}
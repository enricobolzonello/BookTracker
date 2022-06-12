package com.unipd.booktracker.ui.booklist

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
   This ViewModel intermediates between BookListFragment and Room Database
   Database action need to be performed in the IO thread instead of the Main one
*/
class BookListViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDatabase: BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao: BookDao = bookDatabase.bookDao()

    fun getObservableLibrary(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableLibrary()
    }

    fun getObservableWishlist(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableWishlist()
    }

    fun getFilteredLibrary(
        query: String = "",
        notRead: Boolean,
        reading: Boolean,
        read: Boolean,
        orderColumn: String,
        asc: Boolean
    ): List<Book> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getFilteredLibrary(query, notRead, reading, read, OrderColumn.valueOf(orderColumn), asc)
    }

    fun getFilteredWishlist(
        query: String,
        orderColumn: String,
        asc: Boolean
    ): List<Book> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getFilteredWishlist(query, OrderColumn.valueOf(orderColumn), asc)
    }

    fun removeBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.delete(book)
    }
}
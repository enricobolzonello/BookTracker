package com.unipd.booktracker.ui.booklist

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BooklistViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()

    fun getObservableLibrary(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableLibrary()
    }

    fun getObservableWishlist(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableWishlist()
    }

    fun getFilteredLibrary (
        query: String = "",
        notRead: Boolean = true,
        reading: Boolean = true,
        read: Boolean = true,
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ): List<Book> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getFilteredLibrary(query, notRead, reading, read, orderColumn, asc)
    }

    fun getFilteredWishlist(
        query: String = "",
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ): List<Book> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getFilteredWishlist(query, orderColumn, asc)
    }

    fun removeBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.delete(book)
    }
}
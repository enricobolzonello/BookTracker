package com.unipd.booktracker.ui.library

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()

    fun getObservableLibrary(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableLibrary()
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

    fun removeBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.delete(book)
    }
}
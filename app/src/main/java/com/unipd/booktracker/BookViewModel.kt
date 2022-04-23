package com.unipd.booktracker

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.BookRoomDatabase
import com.unipd.booktracker.db.LibraryBook
import com.unipd.booktracker.db.LibraryDao
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val libraryDao : LibraryDao = bookDatabase.libraryDao()

    fun getLibrary() : LiveData<List<LibraryBook>> {
        return libraryDao.getBooksByTitle()
    }

    fun addToLibrary(book : LibraryBook) = viewModelScope.launch {
        libraryDao.insert(book)
    }
}
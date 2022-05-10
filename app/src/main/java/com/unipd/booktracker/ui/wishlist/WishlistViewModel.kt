package com.unipd.booktracker.ui.wishlist

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()

    fun getObservableWishlist(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableWishlist()
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
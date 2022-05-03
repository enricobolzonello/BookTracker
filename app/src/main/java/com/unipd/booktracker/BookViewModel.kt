package com.unipd.booktracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.room.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.util.*


class BookViewModel(application: Application) : AndroidViewModel(application) {

    // The application context is only used to save file in the app-specific directory and show toasts
    private val app = application

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()
    private val readingDao : ReadingDao = bookDatabase.readingDao()

    fun getObservableLibrary(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableLibrary()
    }

    fun getObservableWishlist(): LiveData<List<Book>> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableWishlist()
    }

    fun getObservableReadPages(book: Book): LiveData<Int> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getObservableReadPages(book.id)
    }

    fun countReadPages(book: Book): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking readingDao.countReadPages(book.id)
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

    fun addBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.insert(book)
    }

    fun addBooks(books: List<Book>) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.insert(books)
    }

    fun removeBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.delete(book)
    }

    fun addReadPages(book: Book, pageDifference: Int) = viewModelScope.launch(Dispatchers.IO) {
        Log.i("myLog",pageDifference.toString())
        readingDao.insert(Reading(book.id, Date(), pageDifference))
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
    }

    fun librarySize() : Int = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.countLibraryBooks()
    }

    fun wishlistSize() : Int = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.countWishlistBooks()
    }

    fun clearLibrary() = viewModelScope.launch(Dispatchers.IO) {
        bookDao.deleteLibraryBooks()
    }

    fun clearWishlist() = viewModelScope.launch(Dispatchers.IO) {
        bookDao.deleteWishlistBooks()
    }

    fun clearAll() = viewModelScope.launch(Dispatchers.IO) {
        bookDao.deleteBooks()
    }

    private fun getApiKey(): String? {
        val appInfo = app.packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData.getString("google.books.key")
    }

    suspend fun getBooksFromQuery(query : String) : List<Book> {
        val books : MutableList<Book> = mutableListOf()
        val key = getApiKey()
        if (key.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    app.applicationContext,
                    app.getString(R.string.books_api_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return books
        }
        val url = "https://www.googleapis.com/books/v1/volumes?key=$key&q=$query"
        var response: String? = ""
        try {
            viewModelScope.launch(Dispatchers.IO) {
                response = URL(url).readText()
            }.join()
        } catch (e: Exception) {
            response = null
        }

        if (response.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    app.applicationContext,
                    app.getString(R.string.books_api_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return books
        }

        val items = JSONObject(response!!).getJSONArray("items")
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val itemUrl = item.getString("selfLink")
            getBookInfo(itemUrl)?.let {
                books.add(it)
            }
        }
        return books
    }

    private suspend fun getBookInfo(url: String): Book? {
        var response: String? = ""
        try {
            viewModelScope.launch(Dispatchers.IO) {
                response = URL(url).readText()
            }.join()
        } catch (e: Exception) {
            response = null
        }

        if (response.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    app.applicationContext,
                    app.getString(R.string.books_api_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return null
        }

        val volume = JSONObject(response!!)
        val volumeInfo = volume.getJSONObject("volumeInfo")

        // Check if the minimum required parameters are present
        if (!(volume.has("id") && volumeInfo.has("title") && volumeInfo.has("authors") && volumeInfo.has("pageCount")))
            return null

        val id = volume.getString("id")
        val title = volumeInfo.getString("title")
        val mainAuthor = volumeInfo.getJSONArray("authors").get(0).toString()
        val pages = volumeInfo.getInt("pageCount")

        val publisher =
            if (volumeInfo.has("publisher"))
                    volumeInfo.getString("publisher")
            else
                null

        val isbn =
            if (volumeInfo.has("industryIdentifiers") && volumeInfo.getJSONArray("industryIdentifiers").length() == 2)
                JSONObject(volumeInfo.getJSONArray("industryIdentifiers").get(1).toString()).getString("identifier")
            else
                null

        val mainCategory =
            if (volumeInfo.has("categories"))
                volumeInfo.getJSONArray("categories").get(0).toString()
            else
                null

        val description =
            if (volumeInfo.has("description"))
                volumeInfo.getString("description")
            else
                null

        val year =
            if (volumeInfo.has("publishedDate"))
                volumeInfo.getString("publishedDate").substring(0,4).toInt()
            else
                null

        val language =
            if (volumeInfo.has("language"))
                volumeInfo.getString("language").uppercase()
            else
                null

        var thumbnail: Bitmap? = null
        if (volumeInfo.has("imageLinks") && volumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
            val thumbnailUrl = volumeInfo.getJSONObject("imageLinks").getString("thumbnail")
            // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
            val secureUrl = thumbnailUrl.replaceBefore(":","https")
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    thumbnail = BitmapFactory.decodeStream(URL(secureUrl).openStream())
                }.join()
            } catch (e: Exception) {
                thumbnail = null
            }
        }
        return Book(id, title, mainAuthor, pages, publisher, isbn, mainCategory, description, year, language, thumbnail)
    }
}
package com.unipd.booktracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.room.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*


class BookViewModel(application: Application) : AndroidViewModel(application) {

    // The application context is only used to save file in the app-specific directory and show toasts
    private val app = application

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(app.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()
    private val readingDao : ReadingDao = bookDatabase.readingDao()

    fun getObservableLibrary() : LiveData<List<Book>> {
        return bookDao.getObservableLibrary()
    }

    fun getObservableWishlist() : LiveData<List<Book>> {
        return bookDao.getObservableWishlist()
    }

    fun getFilteredLibrary(
        query: String = "",
        notRead: Boolean = true,
        reading: Boolean = true,
        read: Boolean = true,
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ) : List<Book> {
        return bookDao.getFilteredLibrary(query, notRead, reading, read, orderColumn, asc)
    }

    fun getFilteredWishlist(
        query: String = "",
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ) : List<Book> {
        return bookDao.getFilteredWishlist(query, orderColumn, asc)
    }

    fun addBook(book : Book) = viewModelScope.launch {
        bookDao.insert(book)
    }

    fun getBook(bookId : String) : Book {
        return bookDao.getBook(bookId)
    }

    fun addReadPages(book : Book, pages : Int) = viewModelScope.launch {
        readingDao.insert(Reading(bookId = book.id, date = Date(), pageDifference = pages))
        bookDao.updateReadPages(book.id)
    }

    fun librarySize() : Int {
        return bookDao.countLibraryBooks()
    }

    fun wishlistSize() : Int {
        return bookDao.countWishlistBooks()
    }

    fun clearLibrary() {
        bookDao.deleteLibraryBooks()
    }

    fun clearWishlist() {
        bookDao.deleteWishlistBooks()
    }

    private fun getApiKey(): String? {
        val appInfo = app.packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
        val bundle = appInfo.metaData
        return bundle.getString("google.books.key")
    }

    fun getBooksFromQuery(query : String) : List<Book> {
        val books : MutableList<Book> = mutableListOf()
        val key = getApiKey()
        if (key == null) {
            Toast.makeText(app.applicationContext,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return books
        }
        val url = "https://www.googleapis.com/books/v1/volumes?key=$key&q=$query"
        val response: String
        try {
            response = URL(url).readText()
        } catch (e: IOException) {
            Toast.makeText(app.applicationContext,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return books
        }

        val items = JSONObject(response).getJSONArray("items")
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val itemUrl = item.getString("selfLink")
            getBookInfo(itemUrl)?.let {
                books.add(it)
                addBook(it)
            }
        }
        return books
    }

    private fun getBookInfo(url : String) : Book? {
        val response : String
        try {
            response = URL(url).readText()
        } catch (e: IOException) {
            Toast.makeText(app.applicationContext,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return null
        }
        val volume = JSONObject(response)
        val volumeInfo = volume.getJSONObject("volumeInfo")

        // Check if minimum required parameters are present
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

        val date =
            if (volumeInfo.has("publishedDate"))
                volumeInfo.getString("publishedDate")
            else
                null

        val language =
            if (volumeInfo.has("language"))
                volumeInfo.getString("language")
            else
                null

        val thumbnail =
            if (volumeInfo.has("imageLinks") && volumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
                val thumbnailUrl = volumeInfo.getJSONObject("imageLinks").getString("thumbnail")
                // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
                val secureUrl = thumbnailUrl.replaceBefore(":","https")
                try {
                    BitmapFactory.decodeStream(URL(secureUrl).openStream())
                } catch (e: IOException) {
                    null
                }
            }
            else
                null
        return Book(id, title, mainAuthor, pages, publisher, isbn, mainCategory, description, date, language, thumbnail, 100)
    }
}
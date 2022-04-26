package com.unipd.booktracker

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import androidx.room.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
        notRead: Boolean = true,
        reading: Boolean = true,
        read: Boolean = true,
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ) : List<Book> {
        return bookDao.getFilteredLibrary(notRead, reading, read, orderColumn, asc)
    }

    fun getFilteredWishlist(
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true
    ) : List<Book> {
        return bookDao.getFilteredWishlist(orderColumn, asc)
    }

    fun addBook(book : Book) = viewModelScope.launch {
        bookDao.insert(book)
    }

    fun getBook(bookId : String) : Book {
        return bookDao.getBook(bookId)
    }

    fun addReadPages(book : Book, pages : Int) = viewModelScope.launch {
        readingDao.insert(Reading(bookId = book.id, date = Date(), pageDifference = pages))
        readingDao.updateReadPages(book.id)
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

    fun getBooksFromQuery(query : String) : List<Book> {
        val books : MutableList<Book> = mutableListOf()
        val url = "https://www.googleapis.com/books/v1/volumes?key=AIzaSyAXQ5xBUTifutFOr7ucRNUicUqmO_kTv_g&q=$query"
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

        if (!(volume.has("id") && volumeInfo.has("title") && volumeInfo.has("pageCount")))
            return null
        val id = volume.getString("id")
        val title = volumeInfo.getString("title")
        val pages = volumeInfo.getInt("pageCount")
        val author =
            if (volumeInfo.has("authors"))
                volumeInfo.getJSONArray("authors").get(0).toString()
            else
                "-"
        val publisher = volumeInfo.optString("publisher","-")
        val isbn =
            if (volumeInfo.has("industryIdentifiers"))
                JSONObject(volumeInfo.getJSONArray("industryIdentifiers").get(1).toString()).getString("identifier")
            else
                "-"
        val category =
            if (volumeInfo.has("categories"))
                volumeInfo.getJSONArray("categories").get(0).toString()
            else
                "-"
        val description = volumeInfo.optString("description","-")
        val date = volumeInfo.optString("publishedDate","-")
        val language = volumeInfo.optString("language","-")
        var thumbnail : Bitmap? = null
        if (volumeInfo.has("imageLinks") && volumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
            val thumbnailUrl = volumeInfo.getJSONObject("imageLinks").getString("thumbnail")
            // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
            val secureUrl = thumbnailUrl.replaceBefore(":","https")
            try {
                thumbnail = BitmapFactory.decodeStream(URL(secureUrl).openStream())
            } catch (e: IOException) {
                Toast.makeText(app.applicationContext,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            }
        }
        return Book(id, title, pages, author, publisher, isbn, category, description, date, language, thumbnail, 100)
    }


    /*private fun getBookThumbnail(id: String, url: String) : String {
        val file = File(app.applicationContext.filesDir, "$id.jpg")
        // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
        val secureUrl = url.replaceBefore(":","https")
        val bitmap : Bitmap
        try {
            bitmap = BitmapFactory.decodeStream(URL(secureUrl).openStream())
        } catch (e: IOException) {
            Toast.makeText(app.applicationContext,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return getDefaultThumbnail()
        }
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        return file.absolutePath
    }

    private fun getDefaultThumbnail(): String {
        val file = File(app.applicationContext.filesDir, "default.jpg")
        if (!file.exists()){
            val bitmap = BitmapFactory.decodeResource(app.applicationContext.resources, R.drawable.default_thumbnail)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
        }
        return file.absolutePath
    }*/
}
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

class BookViewModel(application: Application) : AndroidViewModel(application) {

    // The application context is only used to save file in the app-specific directory and show toasts
    private val context = getApplication<Application>()

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao : BookDao = bookDatabase.bookDao()

    fun getObservableLibrary() : LiveData<List<Book>> {
        return bookDao.getObservableLibrary()
    }

    fun getObservableWishlist() : LiveData<List<Book>> {
        return bookDao.getObservableWishlist()
    }

    fun getFilteredLibrary(
        read: Boolean = true,
        reading: Boolean = true,
        notRead: Boolean = true,
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true)
    : List<Book> {
        return bookDao.getFilteredLibrary(read, reading, notRead, orderColumn, asc)
    }

    fun getFilteredWishlist(
        orderColumn: OrderColumns = OrderColumns.title,
        asc: Boolean = true)
    : List<Book> {
        return bookDao.getFilteredWishlist(orderColumn, asc)
    }

    fun addBook(book : Book) = viewModelScope.launch {
        bookDao.insert(book)
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
            Toast.makeText(context,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
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
        val thumbnailPath =
            if (volumeInfo.has("imageLinks") && volumeInfo.getJSONObject("imageLinks").has("thumbnail"))
                getBookThumbnail(id, volumeInfo.getJSONObject("imageLinks").getString("thumbnail"))
            else
                getDefaultThumbnail()
        return Book(id, title, pages, author, publisher, isbn, category, description, date, language, thumbnailPath, 0)
    }


    private fun getBookThumbnail(id: String, url: String) : String {
        val file = File(context.filesDir, "$id.jpg")
        // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
        val secureUrl = url.replaceBefore(":","https")
        val bitmap : Bitmap
        try {
            bitmap = BitmapFactory.decodeStream(URL(secureUrl).openStream())
        } catch (e: IOException) {
            Toast.makeText(context,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return getDefaultThumbnail()
        }
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        return file.absolutePath
    }

    private fun getDefaultThumbnail(): String {
        val file = File(context.filesDir, "default.jpg")
        if (!file.exists()){
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.default_thumbnail)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
        }
        return file.absolutePath
    }
}
package com.unipd.booktracker

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.unipd.booktracker.db.BookRoomDatabase
import com.unipd.booktracker.db.LibraryBook
import com.unipd.booktracker.db.LibraryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class BookViewModel(application: Application) : AndroidViewModel(application) {

    // The application context is only used to save file in the app-specific directory and show toasts
    private val context = getApplication<Application>()

    private val bookDatabase : BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val libraryDao : LibraryDao = bookDatabase.libraryDao()

    fun getObservableLibrary() : LiveData<List<LibraryBook>> {
        return libraryDao.getObservableBooks()
    }

    fun getLibrary() : List<LibraryBook> {
        return libraryDao.getBooksByTitle()
    }

    fun addToLibrary(book : LibraryBook) = viewModelScope.launch {
        libraryDao.insert(book)
    }

    fun clearLibrary() {
        libraryDao.deleteAllBooks()
    }

    fun librarySize() : Int {
        return libraryDao.countBooks()
    }

    fun getBooksFromQuery(query : String) : List<LibraryBook> {
        val books : MutableList<LibraryBook> = mutableListOf()
        val url = "https://www.googleapis.com/books/v1/volumes?key=AIzaSyAXQ5xBUTifutFOr7ucRNUicUqmO_kTv_g&q=$query"

        val response: String
        try {
            response = URL(url).readText()
        } catch (e: IOException) {
            Toast.makeText(context,"An error occurred while connecting to Books API", Toast.LENGTH_SHORT).show()
            return books
        }

        val volumes = JSONObject(response).getJSONArray("items")
        for (i in 0 until volumes.length()) {
            val volume = volumes.getJSONObject(i)
            val volumeInfo = volume.getJSONObject("volumeInfo")
            val id = volume.optString("id","-")
            val title = volumeInfo.optString("title","-")
            val authors : MutableList<String> = mutableListOf()
            if (volumeInfo.has("authors")) {
                val curAuthors = volumeInfo.getJSONArray("authors")
                for (j in 0 until curAuthors.length())
                    authors.add(curAuthors.getString(j))
            }
            val pages = volumeInfo.optInt("pageCount",-1)
            val thumbnailPath =
                if (volumeInfo.has("imageLinks") && volumeInfo.getJSONObject("imageLinks").has("thumbnail"))
                    getBookThumbnail(id, volumeInfo.getJSONObject("imageLinks").getString("thumbnail"))
                else
                    getDefaultThumbnail()
            books.add(LibraryBook(id, title, authors, pages, thumbnailPath))
            addToLibrary(LibraryBook(id, title, authors, pages, thumbnailPath))
        }
        return books
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
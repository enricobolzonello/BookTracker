package com.unipd.booktracker.ui.addbook

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import com.unipd.booktracker.BuildConfig
import com.unipd.booktracker.db.*
import com.unipd.booktracker.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

/*
   This ViewModel intermediates between AddBookFragment and Google Books API
   Network requests need to performed in the IO thread instead of the Main one
*/
class AddBookViewModel(application: Application) : AndroidViewModel(application) {

    suspend fun getBooksFromQuery(query: String): List<Book>? {
        val key = BuildConfig.GOOGLE_BOOKS_KEY
        if (key.isBlank())
            return null

        val url = "https://www.googleapis.com/books/v1/volumes?key=$key&q=$query"
        var response: String? = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                response = URL(url).readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.join()

        if (response.isNullOrBlank())
            return null
        val data = JSONObject(response!!)
        val books: MutableList<Book> = mutableListOf()
        if (data.has("totalItems") && data.getInt("totalItems") <= 0)
            return books

        val items = data.getJSONArray("items")
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
        var response: String? = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                response = URL(url).readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.join()

        if (response.isNullOrBlank())
            return null

        val volume = JSONObject(response!!)
        val volumeInfo = volume.getJSONObject("volumeInfo")

        // Check if the minimum required parameters are present
        if (!(volume.has("id") && volumeInfo.has("title") && volumeInfo.has("authors")
                    && volumeInfo.getJSONArray("authors").length() >= 1 && volumeInfo.has("pageCount"))
        )
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
            if (volumeInfo.has("industryIdentifiers") && volumeInfo.getJSONArray("industryIdentifiers").length() >= 2
                && JSONObject(volumeInfo.getJSONArray("industryIdentifiers").get(1).toString()).has("identifier")
            )
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
                // Description string may contain html tags
                HtmlCompat.fromHtml(volumeInfo.getString("description"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            else
                null

        val year =
            if (volumeInfo.has("publishedDate"))
                volumeInfo.getString("publishedDate").substring(0, 4).toInt()
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
                .replace("http", "https") // Cleartext HTTP traffic is not permitted, so secure url (https) is needed
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    thumbnail = BitmapFactory.decodeStream(URL(thumbnailUrl).openStream())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.join()
        }

        return Book(
            id,
            title,
            mainAuthor,
            pages,
            publisher,
            isbn,
            mainCategory,
            description,
            year,
            language,
            thumbnail.toByteArray()
        )
    }
}
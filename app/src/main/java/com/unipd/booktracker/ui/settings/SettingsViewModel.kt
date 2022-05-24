package com.unipd.booktracker.ui.settings

import android.app.Application
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.*
import com.unipd.booktracker.R
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.*

/*
   This ViewModel intermediates between SettingsFragment and Room Database and Storage
   Database and Storage action need to be performed in the IO thread instead of the Main one
*/
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // The application context is only used to get resources and show toasts
    private val app = getApplication<Application>()

    private val bookDatabase: BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val bookDao: BookDao = bookDatabase.bookDao()
    private val readingDao: ReadingDao = bookDatabase.readingDao()

    private fun getBooks(): List<Book> = runBlocking(Dispatchers.IO) {
        return@runBlocking bookDao.getBooks()
    }

    private fun getReadings(): List<Reading> = runBlocking(Dispatchers.IO) {
        return@runBlocking readingDao.getReadings()
    }

    private fun addBook(book: Book) = viewModelScope.launch(Dispatchers.IO) {
        bookDao.insert(book)
    }

    private fun addReading(reading: Reading) = viewModelScope.launch(Dispatchers.IO) {
        readingDao.insert(reading)
    }

    fun clearBooks() = viewModelScope.launch(Dispatchers.IO) {
        bookDao.deleteBooks()
    }

    fun exportDbToFile(): String? = runBlocking(Dispatchers.IO) {
        val books = getBooks()
        val readings = getReadings()

        val exportFile: File?
        var fos: FileOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            exportFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                app.getString(R.string.app_name) + ".dat"
            )
            fos = FileOutputStream(exportFile)
            oos = ObjectOutputStream(fos)

            books.forEach { oos.writeObject(it) }
            readings.forEach { oos.writeObject(it) }
            return@runBlocking exportFile.absolutePath
        } catch (e: Exception) {
            Toast.makeText(app, app.getString(R.string.file_exported_error), Toast.LENGTH_SHORT).show()
            return@runBlocking null
        } finally {
            fos?.close()
            oos?.close()
        }
    }

    fun importDbFromFile(filePath: String): Boolean = runBlocking(Dispatchers.IO) {
        val books = mutableListOf<Book>()
        val readings = mutableListOf<Reading>()

        var fis: FileInputStream? = null
        var ois: ObjectInputStream? = null
        try {
            fis = FileInputStream(filePath)
            ois = ObjectInputStream(fis)

            while (fis.available() > 0) {
                val obj = ois.readObject()
                if (obj !is Book && obj !is Reading)
                    throw Exception()
                if (obj is Book)
                    books.add(obj)
                if (obj is Reading)
                    readings.add(obj)
            }

            books.forEach { addBook(it) }
            readings.forEach { addReading(it) }
            return@runBlocking true
        } catch (e: Exception) {
            Toast.makeText(app, app.getString(R.string.file_imported_error), Toast.LENGTH_SHORT).show()
            return@runBlocking false
        } finally {
            fis?.close()
            ois?.close()
        }
    }
}
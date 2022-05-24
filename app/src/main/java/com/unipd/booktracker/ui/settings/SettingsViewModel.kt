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
import kotlinx.coroutines.withContext
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

    suspend fun exportDbToFile(): String? {
        var exportFile: File? = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                exportFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    app.getString(R.string.app_name) + ".dat"
                )
                val oos = ObjectOutputStream(FileOutputStream(exportFile))
                getBooks().forEach { oos.writeObject(it) }
                getReadings().forEach { oos.writeObject(it) }
                oos.close()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, app.getString(R.string.file_exported_error), Toast.LENGTH_SHORT).show()
                }
            }
        }.join()
        return exportFile?.absolutePath
    }

    suspend fun importDbFromFile(filePath: String): Boolean {
        var imported = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fis = FileInputStream(filePath)
                val ois = ObjectInputStream(fis)
                while (fis.available() > 0) {
                    val obj = ois.readObject()
                    if (obj !is Book && obj !is Reading)
                        throw Exception()
                    if (obj is Book)
                        addBook(obj)
                    if (obj is Reading)
                        addReading(obj)
                }
                fis.close()
                ois.close()
                imported = true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, app.getString(R.string.file_imported_error), Toast.LENGTH_SHORT).show()
                }
            }
        }.join()
        return imported
    }
}
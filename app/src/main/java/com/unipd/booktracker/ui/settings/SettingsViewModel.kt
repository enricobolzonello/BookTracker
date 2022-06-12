package com.unipd.booktracker.ui.settings

import android.app.Application
import android.os.Environment
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
                var count = 1
                while (count == 1 || exportFile!!.exists()) {
                    exportFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "${getApplication<Application>().getString(R.string.app_name)}Export ($count).dat"
                    )
                    count += 1
                }

                val oos = ObjectOutputStream(FileOutputStream(exportFile))
                getBooks().forEach { oos.writeObject(it) }
                getReadings().forEach { oos.writeObject(it) }
                oos.close()
            } catch (e: Exception) {
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }.join()
        return imported
    }
}
package com.unipd.booktracker.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.unipd.booktracker.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
   This ViewModel intermediates between StatsFragment and Room Database
   Database action need to be performed in the IO thread instead of the Main one
*/
class StatsViewModel(application: Application): AndroidViewModel(application) {
    private val bookDatabase: BookRoomDatabase = BookRoomDatabase.getDatabase(application.applicationContext)
    private val statsDao: StatsDao = bookDatabase.statsDao()

    fun countReadPages(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countReadPages()
    }

    fun countReadPagesToday(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countReadPages(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    fun avgReadPagesByDay(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.avgReadPagesByDay(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    fun countReadBooks(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countReadBooks()
    }

    fun countReadBooksThisYear(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countReadBooks(LocalDate.now().year.toString())
    }

    fun avgReadBooksByYear(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.avgReadBooksByYear(LocalDate.now().year.toString())
    }

    fun mostReadAuthor(): String? = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.mostReadAuthor()
    }

    fun countAuthors(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countAuthors()
    }

    fun mostReadCategory(): String? = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.mostReadCategory()
    }

    fun countCategories(): Int = runBlocking(Dispatchers.IO) {
        return@runBlocking statsDao.countCategories()
    }
}
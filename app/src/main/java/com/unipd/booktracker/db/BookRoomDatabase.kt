package com.unipd.booktracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [Book::class, Reading::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BookRoomDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingDao(): ReadingDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: BookRoomDatabase? = null

        fun getDatabase(context: Context): BookRoomDatabase {
            // if the INSTANCE is not null, then return it, if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, BookRoomDatabase::class.java, "book_database"
                )
                .addCallback(updateReadPages)
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private val updateReadPages = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("CREATE TRIGGER update_read_pages " +
                        "AFTER INSERT ON readings " +
                        "WHEN (SELECT EXISTS (SELECT * FROM books WHERE id = NEW.bookId)) " +
                        "BEGIN " +
                            "UPDATE books " +
                            "SET readPages = (SELECT SUM(pageDifference) FROM readings WHERE bookId = NEW.bookId) " +
                            "WHERE books.id = NEW.bookId; " +
                        "END")
            }
        }
    }
}
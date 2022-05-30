package com.unipd.booktracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Book::class, Reading::class], version = 1, exportSchema = false)
abstract class BookRoomDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingDao(): ReadingDao
    abstract fun statsDao(): StatsDao

    // Singleton prevents multiple instances of database opening at the same time
    companion object {
        @Volatile
        private var INSTANCE: BookRoomDatabase? = null

        fun getDatabase(context: Context): BookRoomDatabase {
            // The instance is returned if it's not null, otherwise it's created
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context.applicationContext, BookRoomDatabase::class.java, "book_database")
                        .addCallback(dbTriggers)
                        .build()
                INSTANCE = instance
                instance
            }
        }

        private val dbTriggers = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // When a new reading record is inserted, update the readPages column
                db.execSQL(
                    "CREATE TRIGGER read_pages_insert " +
                            "AFTER INSERT ON readings " +
                            "WHEN (SELECT EXISTS (SELECT * FROM books WHERE id = NEW.bookId)) " +
                            "BEGIN " +
                            "UPDATE books " +
                            "SET readPages = (SELECT SUM(pageDifference) FROM readings WHERE bookId = NEW.bookId) " +
                            "WHERE books.id = NEW.bookId; " +
                            "END"
                )

                // When a new reading record is updated, update the readPages column
                db.execSQL(
                    "CREATE TRIGGER read_pages_update " +
                            "AFTER UPDATE ON readings " +
                            "WHEN (SELECT EXISTS (SELECT * FROM books WHERE id = NEW.bookId)) " +
                            "BEGIN " +
                            "UPDATE books " +
                            "SET readPages = (SELECT SUM(pageDifference) FROM readings WHERE bookId = NEW.bookId) " +
                            "WHERE books.id = NEW.bookId; " +
                            "END"
                )
            }
        }
    }
}
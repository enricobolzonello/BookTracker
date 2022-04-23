package com.unipd.booktracker.db

import androidx.room.TypeConverter
import org.json.JSONArray

class TypeConverter {

    @TypeConverter
    fun fromAuthorsList(authorsList: List<String>) : String {
        return JSONArray(authorsList).toString()
    }

    @TypeConverter
    fun toAuthorsList(authorsString: String): List<String> {
        val authorsList = mutableListOf<String>()
        val authorsArray = JSONArray(authorsString)
        for (i in 0 until authorsArray.length())
            authorsList.add(authorsArray[i].toString())
        return authorsList
    }
}
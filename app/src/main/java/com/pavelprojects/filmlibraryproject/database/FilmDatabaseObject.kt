package com.pavelprojects.filmlibraryproject.database

import android.content.Context
import androidx.room.Room

object FilmDatabaseObject  {
    private var INSTANCE: FilmDatabase? = null

    fun getInstance(context: Context): FilmDatabase? {
        if (INSTANCE == null) {
            synchronized(FilmDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    FilmDatabase::class.java, "filmDatabase.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(FilmDatabaseCallback(context))
                    .build()
            }
        }
        return INSTANCE
    }

    fun destroyInstance() {
        INSTANCE?.close()
        INSTANCE = null
    }
}
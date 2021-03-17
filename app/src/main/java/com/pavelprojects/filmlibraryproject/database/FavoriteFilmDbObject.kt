package com.pavelprojects.filmlibraryproject.database

import android.content.Context
import androidx.room.Room

object FavoriteFilmDbObject {
    private var INSTANCE: FavoriteFilmDatabase? = null

    fun getInstance(context: Context): FavoriteFilmDatabase? {
        if (INSTANCE == null) {
            synchronized(FavoriteFilmDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    FavoriteFilmDatabase::class.java, "favFilmDatabase.db"
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
package com.pavelprojects.filmlibraryproject.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pavelprojects.filmlibraryproject.FilmItem
import com.pavelprojects.filmlibraryproject.dao.FilmItemDao

@Database(entities = [FilmItem::class], version = 3, exportSchema = false)
abstract class FilmDatabase : RoomDatabase() {
    abstract fun getFilmItemDao(): FilmItemDao
}
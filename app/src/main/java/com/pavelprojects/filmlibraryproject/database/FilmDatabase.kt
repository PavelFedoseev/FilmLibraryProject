package com.pavelprojects.filmlibraryproject.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pavelprojects.filmlibraryproject.database.dao.ChangedItemDao
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

@Database(entities = [FilmItem::class, ChangedFilmItem::class], version = 5, exportSchema = false)
abstract class FilmDatabase : RoomDatabase() {
    abstract fun getFilmItemDao(): FilmItemDao
    abstract fun getChangedItemDao(): ChangedItemDao
}
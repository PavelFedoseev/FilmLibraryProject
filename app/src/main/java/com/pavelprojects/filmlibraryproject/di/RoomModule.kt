package com.pavelprojects.filmlibraryproject.di

import android.app.Application
import androidx.room.Room
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseCallback
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule(application: Application) {
    private val filmDatabase: FilmDatabase = Room.databaseBuilder(
        application,
        FilmDatabase::class.java, "filmDatabase.db"
    )
        .fallbackToDestructiveMigration()
        .addCallback(FilmDatabaseCallback(application))
        .build()
    @Singleton
    @Provides
    fun provideFilmDatabase() = filmDatabase

    @Singleton
    @Provides
    fun providesFilmItemDao() = filmDatabase.getFilmItemDao()

    @Singleton
    @Provides
    fun providesChangedItemDao() = filmDatabase.getChangedItemDao()
}
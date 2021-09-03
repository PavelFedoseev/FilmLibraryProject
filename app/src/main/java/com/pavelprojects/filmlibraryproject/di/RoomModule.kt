package com.pavelprojects.filmlibraryproject.di

import androidx.room.Room
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule constructor(val application: App) {
    private val filmDatabase: FilmDatabase = Room.databaseBuilder(
        application,
        FilmDatabase::class.java, "filmDatabase.db"
    )
        .fallbackToDestructiveMigration()
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

    @Singleton
    @Provides
    fun providesFilmRepository() = FilmRepository()



}
package com.pavelprojects.filmlibraryproject.di

import android.app.Application
import com.google.android.datatransport.runtime.dagger.Component
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity
import javax.inject.Singleton

@Singleton
@Component( modules = [AppModule::class, RoomModule::class])
interface AppComponent {
    fun inject(mainActivity: FilmLibraryActivity)
    fun productDao(): FilmItemDao
    fun demoDatabase(): FilmDatabase
    fun productRepository(): FilmRepository
    fun application(): Application
}
package com.pavelprojects.filmlibraryproject.di

import android.app.Application
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(dependencies = [], modules = [AppModule::class, RoomModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun productDao(): FilmItemDao
    fun demoDatabase(): FilmDatabase
    fun productRepository(): FilmRepository
    fun application(): Application
}
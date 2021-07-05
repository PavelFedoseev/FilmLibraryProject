package com.pavelprojects.filmlibraryproject.di

import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.network.RetroApi
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.watchlater.WatchLaterFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component( dependencies = [], modules = [AppModule::class, RoomModule::class, NetworkModule::class])
interface AppComponent {
    fun inject(filmLibActivity: FilmLibraryActivity)
    fun inject(filmRepository: FilmRepository)
    fun inject(filmListFragment: FilmListFragment)
    fun inject(filmInfoFragment: FilmInfoFragment)
    fun inject(favoriteFilmsFragment: FavoriteFilmsFragment)
    fun inject(watchLaterFragment: WatchLaterFragment)
    fun filmItemDao(): FilmItemDao
    fun filmDatabase(): FilmDatabase
    fun filmRepository(): FilmRepository
    fun application(): App
    fun notificationRepo(): NotificationRepository
    fun retroApi(): RetroApi
}
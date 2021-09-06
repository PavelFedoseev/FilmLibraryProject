package com.pavelprojects.filmlibraryproject.di

import android.os.Build
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.network.FilmDataPagingSource
import com.pavelprojects.filmlibraryproject.network.RetroApi
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val application: App) {

    @Singleton
    @Provides
    fun provideApplication() = application

    @Singleton
    @Provides
    fun provideNotificationRepository() = NotificationRepository(application)

    @Singleton
    @Provides
    fun provideFilmDataPagingSource(filmDatabase: FilmDatabase, retroApi: RetroApi): FilmDataPagingSource{
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.applicationContext.resources.configuration.locales[0].language
        } else {
            application.applicationContext.resources.configuration.locale.language
        }
        return FilmDataPagingSource.createSource(filmDatabase, retroApi, languageCode)
    }

}
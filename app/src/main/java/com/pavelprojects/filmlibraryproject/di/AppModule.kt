package com.pavelprojects.filmlibraryproject.di

import com.pavelprojects.filmlibraryproject.App
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
}
package com.pavelprojects.filmlibraryproject.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FavoriteViewModel
import com.pavelprojects.filmlibraryproject.ui.viewmodel.WatchLaterViewModel
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FilmInfoViewModel
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FilmLibraryViewModel
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val app: App,
    private val repository: FilmRepository,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FilmLibraryViewModel::class.java) -> {
                FilmLibraryViewModel(app, repository, notificationRepository) as T
            }
            modelClass.isAssignableFrom(WatchLaterViewModel::class.java) -> {
                WatchLaterViewModel(app, repository, notificationRepository) as T
            }
            modelClass.isAssignableFrom(FilmInfoViewModel::class.java) -> {
                FilmInfoViewModel(app, repository, notificationRepository) as T
            }
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                FavoriteViewModel(app, repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
package com.pavelprojects.filmlibraryproject.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.ui.vm.ChangedViewModel
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
import javax.inject.Inject

class ViewModelFactory @Inject constructor(private val app: App, private val repository: FilmRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FilmLibraryViewModel::class.java) -> {
                FilmLibraryViewModel(app, repository) as T
            }
            modelClass.isAssignableFrom(ChangedViewModel::class.java) -> {
                ChangedViewModel(app, repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
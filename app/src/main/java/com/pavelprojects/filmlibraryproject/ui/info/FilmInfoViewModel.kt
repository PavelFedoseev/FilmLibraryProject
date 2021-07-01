package com.pavelprojects.filmlibraryproject.ui.info

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

class FilmInfoViewModel(app: App) : AndroidViewModel(app) {
    val filmById: MutableLiveData<FilmItem> = MutableLiveData()
}
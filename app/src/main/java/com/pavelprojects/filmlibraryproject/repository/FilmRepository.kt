package com.pavelprojects.filmlibraryproject.repository

import android.app.Application
import android.os.Build
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.dao.ChangedItemDao
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.network.FilmDataResponse
import com.pavelprojects.filmlibraryproject.network.RetroApi
import io.reactivex.MaybeObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilmRepository @Inject constructor(val application: Application) {

    companion object {
        const val TAG_FILM_REPO = "FilmRepository"
        const val CODE_FILM_TABLE = 1
    }

    var loadedPage: Int = 1
    var recFilmListPos: Int = 0
    var recChangedPos: Int = 0

    init {
        App.appComponent.inject(this)
    }

    @Inject
    lateinit var filmItemDao: FilmItemDao

    @Inject
    lateinit var changedItemDao: ChangedItemDao

    @Inject
    lateinit var retroApi: RetroApi

    fun insert(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.insert(filmItem)
        else changedItemDao.insert(filmItem.toChangedFilmItem())
    }

    fun updateChanged(changedFilmItem: ChangedFilmItem) {
        changedItemDao.update(changedFilmItem)
    }

    fun insertAll(listOfFilms: List<FilmItem>, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.insertAll(listOfFilms)
        else changedItemDao.insertAllFav(listOfFilms.map { it.toChangedFilmItem() })
    }

    fun update(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.update(filmItem)
        else changedItemDao.update(filmItem.toChangedFilmItem())
    }

    fun getAllFilms(observer: MaybeObserver<List<FilmItem>>) {
        filmItemDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun getFavFilms(observer: MaybeObserver<List<FilmItem>>) {
        changedItemDao.getAllFav()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun getWatchLaterFilms(observer: MaybeObserver<List<ChangedFilmItem>>) {
        changedItemDao.getAllWatchLater()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun getAllChanged(observer: MaybeObserver<List<ChangedFilmItem>>) {
        changedItemDao.getAllChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }



    fun getFilmById(id: Int, observer: MaybeObserver<FilmItem>) {
        filmItemDao
            .getById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    fun delete(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.delete(filmItem)
        else changedItemDao.delete(filmItem.toChangedFilmItem())
    }

    fun deleteAll(code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.deleteAllFilms()
        else changedItemDao.deleteAllChangedFilms()
    }

    fun getPopularMovies(page: Int, observer: SingleObserver<FilmDataResponse>) {
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.applicationContext.resources.configuration.locales[0].language
        } else {
            application.applicationContext.resources.configuration.locale.language
        }
        retroApi.getPopularMovies(page = page, language = languageCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

    }

}
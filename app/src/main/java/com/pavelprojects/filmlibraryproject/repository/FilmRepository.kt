package com.pavelprojects.filmlibraryproject.repository

import android.os.Build
import android.util.Log
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
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilmRepository @Inject constructor(val application: App) {

    companion object {
        const val TAG_FILM_REPO = "FilmRepository"
        const val CODE_FILM_TABLE = 1
    }

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

    fun getAllFilms(callback: FilmListResponseCallback) {
        filmItemDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(maybeObserver(callback))
    }

    fun getFavFilms(callback: FilmListResponseCallback) {
        changedItemDao.getAllFav()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(maybeObserver(callback))
    }

    fun getWatchLaterFilms(callback: ChangedFilmListResponseCallback) {
        changedItemDao.getAllWatchLater()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(maybeObserver(callback))
    }

    fun getAllChanged(callback: ChangedFilmListResponseCallback) {
        changedItemDao.getAllChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(maybeObserver(callback))
    }

    private fun maybeObserver(callback: FilmListResponseCallback) = object : MaybeObserver<List<FilmItem>> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onSuccess(t: List<FilmItem>) {
            callback.onSuccess(t)
        }

        override fun onError(e: Throwable) {
        }

        override fun onComplete() {
        }
    }

    private fun maybeObserver(callback: ChangedFilmListResponseCallback) =
        object : MaybeObserver<List<ChangedFilmItem>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onSuccess(t: List<ChangedFilmItem>) {
                callback.onSuccess(t)
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }

        }

    fun getFilmById(id: Long): FilmItem? {
        return filmItemDao.getById(id)
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

    fun getPopularMovies(page: Int, listener: PopularMoviesResponseListener) {
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.applicationContext.resources.configuration.locales[0].language
        } else {
            application.applicationContext.resources.configuration.locale.language
        }
        retroApi.getPopularMovies(page = page, language = languageCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<FilmDataResponse>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: FilmDataResponse) {
                    listener.onSuccess(t)
                    Log.d(TAG_FILM_REPO, "Movies: ${t.movies}")
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG_FILM_REPO, "Failed to get response")
                }
            })

    }

    interface FilmListResponseCallback {
        fun onSuccess(list: List<FilmItem>)
    }

    interface ChangedFilmListResponseCallback {
        fun onSuccess(list: List<ChangedFilmItem>)
    }

    interface PopularMoviesResponseListener {
        fun onSuccess(data: FilmDataResponse?)
        fun onFailure()
    }

}
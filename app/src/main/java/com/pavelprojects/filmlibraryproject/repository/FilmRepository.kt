package com.pavelprojects.filmlibraryproject.repository

import android.os.Build
import android.util.Log
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseObject
import com.pavelprojects.filmlibraryproject.database.dao.ChangedItemDao
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.network.FilmDataResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilmRepository() {

    companion object {
        const val TAG_FILM_REPO = "FilmRepository"
        const val CODE_FILM_DB = 1
        const val CODE_FAV_FILM_DB = 2
    }


    private var filmItemDao: FilmItemDao? =
            FilmDatabaseObject.getInstance(App.instance)?.getFilmItemDao()

    private var changedItemDao: ChangedItemDao? =
        FilmDatabaseObject.getInstance(App.instance)?.getChangedItemDao()

    fun insert(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.insert(filmItem)
        else changedItemDao?.insert(filmItem.toChangedFilmItem())
    }

    fun insertAll(listOfFilms: List<FilmItem>, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.insertAll(listOfFilms)
        else changedItemDao?.insertAllFav(listOfFilms.map { it.toChangedFilmItem() })
    }

    fun insertAllGetFilms(listOfFilms: List<FilmItem>): List<FilmItem>? {
        filmItemDao?.insertAll(listOfFilms)
        return filmItemDao?.getAll()
    }

    fun update(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.update(filmItem)
        else changedItemDao?.update(filmItem.toChangedFilmItem())
    }

    fun getAllFilms(): List<FilmItem>? {
        return filmItemDao?.getAll()
    }

    fun getFavFilms(): List<FilmItem>? {
        return changedItemDao?.getAllFav()
    }

    fun getFilmById(id: Long): FilmItem? {
        return filmItemDao?.getById(id)
    }

    fun delete(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.delete(filmItem)
        else changedItemDao?.delete(filmItem.toChangedFilmItem())
    }

    fun deleteAll(code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.deleteAllFilms()
        else changedItemDao?.deleteAllFavFilms()
    }

    fun getPopularMovies(page: Int, listener: PopularMoviesResponseListener) {
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            App.instance.applicationContext.resources.configuration.locales[0].language
        } else {
            App.instance.applicationContext.resources.configuration.locale.language
        }
        App.instance.api.getPopularMovies(page = page, language = languageCode)
                .enqueue(object : Callback<FilmDataResponse> {
                    override fun onFailure(call: Call<FilmDataResponse>, t: Throwable) {
                        listener.onFailure()
                        Log.e(TAG_FILM_REPO, t.toString())
                    }

                    override fun onResponse(
                        call: Call<FilmDataResponse>,
                        response: Response<FilmDataResponse>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()

                            if (responseBody != null) {
                                listener.onSuccess(response.body())
                                Log.d(TAG_FILM_REPO, "Movies: ${responseBody.movies}")
                            } else {
                                listener.onFailure()
                                Log.d(TAG_FILM_REPO, "Failed to get response")
                            }
                        }
                    }
                })

    }

    interface PopularMoviesResponseListener {
        fun onSuccess(data: FilmDataResponse?)
        fun onFailure()
    }

}
package com.pavelprojects.filmlibraryproject.repository

import android.os.Build
import android.util.Log
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.FilmItem
import com.pavelprojects.filmlibraryproject.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.FavoriteFilmDbObject
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseObject
import com.pavelprojects.filmlibraryproject.retrofit.FilmDataResponse
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
    private var favFilmDao: FilmItemDao? =
            FavoriteFilmDbObject.getInstance(App.instance)?.getFilmItemDao()

    fun insert(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.insert(filmItem)
        else favFilmDao?.insert(filmItem)
    }

    fun insertAll(listOfFilms: List<FilmItem>, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.insertAll(listOfFilms)
        else favFilmDao?.insertAll(listOfFilms)
    }

    fun insertAllGetFilms(listOfFilms: List<FilmItem>): List<FilmItem>? {
        filmItemDao?.insertAll(listOfFilms)
        return filmItemDao?.getAll()
    }

    fun update(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.update(filmItem)
        else favFilmDao?.update(filmItem)
    }

    fun getAllFilms(): List<FilmItem>? {
        return filmItemDao?.getAll()
    }

    fun getFavFilms(): List<FilmItem>? {
        return favFilmDao?.getAll()
    }

    fun getFilmById(id: Long, code: Int): FilmItem? {
        return if (code == CODE_FILM_DB)
            filmItemDao?.getById(id)
        else favFilmDao?.getById(id)
    }

    fun delete(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.delete(filmItem)
        else favFilmDao?.delete(filmItem)
    }

    fun deleteAll(code: Int) {
        if (code == CODE_FILM_DB)
            filmItemDao?.deleteAllFilms()
        else favFilmDao?.deleteAllFilms()
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
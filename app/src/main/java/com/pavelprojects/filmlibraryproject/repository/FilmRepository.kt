package com.pavelprojects.filmlibraryproject.repository

import android.app.Application
import android.os.Build
import android.util.Log
import com.pavelprojects.filmlibraryproject.FilmItem
import com.pavelprojects.filmlibraryproject.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseObject
import com.pavelprojects.filmlibraryproject.retrofit.FilmDataResponse
import com.pavelprojects.filmlibraryproject.retrofit.RetroApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FilmRepository(val application: Application) {

    private val api: RetroApi

    companion object{
        const val TAG_FILM_REPO = "FilmRepository"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(RetroApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RetroApi::class.java)
    }
    private var filmItemDao: FilmItemDao? =
        FilmDatabaseObject.getInstance(application)?.getFilmItemDao()

    fun insert(filmItem: FilmItem) {
        filmItemDao?.insert(filmItem)
    }

    fun insertAll(listOfFilms: List<FilmItem>) {
        filmItemDao?.insertAll(listOfFilms)
    }

    fun update(filmItem: FilmItem) {
        filmItemDao?.update(filmItem)
    }

    fun getAllFilms(): List<FilmItem>? {
        return filmItemDao?.getAll()
    }

    fun getFilmById(id: Long): FilmItem? {
        return filmItemDao?.getById(id)
    }

    fun delete(filmItem: FilmItem) {
        filmItemDao?.delete(filmItem)
    }

    fun deleteAll() {
        filmItemDao?.deleteAllFilms()
    }

    fun getPopularMovies(page: Int = 1, listener: PopularMoviesResponseListener){
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.applicationContext.resources.configuration.locales[0].language
        } else {
            application.applicationContext.resources.configuration.locale.language
        }
            api.getPopularMovies(page = page, language = languageCode).enqueue(object: Callback<FilmDataResponse> {
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

    interface PopularMoviesResponseListener{
        fun onSuccess(data : FilmDataResponse?)
        fun onFailure()
    }

}
package com.pavelprojects.filmlibraryproject.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroApi {
    companion object{
        const val FILTER_TMDB_POPUlAR = "popularity.desc"
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY_TMDB = "473d343bb4af4dc7e510821907ee4e99"
    }

    @GET("discover/movie")
    fun getPopularMovies(
        @Query("api_key") apiKey: String = API_KEY_TMDB,
        @Query("sort_by") sortBy: String = FILTER_TMDB_POPUlAR,
        @Query("language") language: String,
        @Query("page") page: Int
    ) : Single<FilmDataResponse>

}
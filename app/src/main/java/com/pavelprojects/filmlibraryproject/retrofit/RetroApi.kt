package com.pavelprojects.filmlibraryproject.retrofit

import com.pavelprojects.filmlibraryproject.API_KEY_TMDB
import com.pavelprojects.filmlibraryproject.FILTER_TMDB_POPUlAR
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroApi {
    companion object{
        const val BASE_URL = "https://www.themoviedb.org"
    }

    @GET("/discover/movie")
    fun getPopularMovies(
        @Query("api_key") apiKey: String = API_KEY_TMDB,
        @Query("sort_by") sortBy: String = FILTER_TMDB_POPUlAR,
        @Query("language") language: String,
        @Query("page") page: Int
    ) : Call<FilmDataResponse>

}
package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.util.Log
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.network.RetroApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class App : Application() {
    companion object{
        const val TAG_APP = "App"
        lateinit var instance: App
        private set
    }

    lateinit var api: RetroApi
    lateinit var repository: FilmRepository
    var loadedPage: Int = 1
    var recFilmListPos: Int = 0
    var recFavPos: Int = 0
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG_APP, "$TAG_APP onCreate")
        instance = this
        initRetrofit()
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(RetroApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS).build()
            )
            .build()
        api = retrofit.create(RetroApi::class.java)
        repository = FilmRepository()
    }

}
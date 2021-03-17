package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.util.Log
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseObject
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.retrofit.RetroApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class App : Application() {
    companion object{
        const val TAG_APP = "App"
        lateinit var instance: App
        private set
    }

    lateinit var api: RetroApi
    lateinit var repository: FilmRepository
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
                OkHttpClient().newBuilder().connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS).build()
            )
            .build()
        api = retrofit.create(RetroApi::class.java)
        repository = FilmRepository()
    }

}
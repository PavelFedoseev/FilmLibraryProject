package com.pavelprojects.filmlibraryproject.di

import com.pavelprojects.filmlibraryproject.network.RetroApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun providesRetroApi(): RetroApi{
        val retrofit = Retrofit.Builder()
            .baseUrl(RetroApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(
                OkHttpClient().newBuilder().connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS).build()
            )
            .build()
        return retrofit.create(RetroApi::class.java)
    }
}
package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.util.Log
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.FilmDatabaseObject
import java.util.concurrent.Executors

class App : Application() {
    companion object{
        const val TAG_APP = "App"
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG_APP, "$TAG_APP onCreate")
    }
}
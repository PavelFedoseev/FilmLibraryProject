package com.pavelprojects.filmlibraryproject.database
import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class FilmDatabaseCallback(private val ctx: Context) : RoomDatabase.Callback() {
    companion object{
        const val TAG_CALLBACK = "FilmDatabaseCallback"
    }
    override fun onCreate(db: SupportSQLiteDatabase) {
        Log.d(TAG_CALLBACK, "$TAG_CALLBACK onCreate")
        Executors.newSingleThreadScheduledExecutor().execute(Runnable {
            //FilmDatabaseObject.getInstance(ctx)?.getFilmItemDao()?.insert(publisher)
            Log.d(TAG_CALLBACK, "$TAG_CALLBACK executed")
        })
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        // do something every time database is open
    }

}
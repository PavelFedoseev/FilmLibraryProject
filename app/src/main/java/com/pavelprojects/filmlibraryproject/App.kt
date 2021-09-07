package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.pavelprojects.filmlibraryproject.di.*
import timber.log.Timber


class App : Application() {
    companion object {

        const val REMINDER_CHANNEL_ID = "reminder channel"
        const val TAG_APP = "App"
        const val LINK_GITHUB = "https://github.com/PavelFedoseev"
        const val LINK_PROFILE = "https://github.com/PavelFedoseev"
        lateinit var appComponent: AppComponent
    }


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.tag(TAG_APP).d(TAG_APP + " onCreate")
        Runtime.getRuntime().exec("logcat -d FrameEvents:S mlibraryprojec:S")
        initDagger()
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .roomModule(RoomModule(this))
            .networkModule(NetworkModule())
            .build()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance)
            mChannel.description = descriptionText

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

}
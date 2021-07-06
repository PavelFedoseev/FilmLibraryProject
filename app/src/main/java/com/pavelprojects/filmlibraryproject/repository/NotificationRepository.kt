package com.pavelprojects.filmlibraryproject.repository

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pavelprojects.filmlibraryproject.broadcast.ReminderBroadcast
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import java.util.*
import javax.inject.Inject

class NotificationRepository @Inject constructor(val app: Application) : NotificationUpdater {
    private val alarmManager =
        app.getSystemService(AppCompatActivity.ALARM_SERVICE) as? AlarmManager

    override fun updateNotificationChannel(list: List<ChangedFilmItem>) {
        for (item in list) {
            if (item.watchLaterDate != -1L && item.watchLaterDate >= Calendar.getInstance().timeInMillis) {
                val intent = Intent(app, ReminderBroadcast::class.java)
                val bundle =
                    Bundle().apply { putParcelable(ReminderBroadcast.BUNDLE_FILMITEM, item) }
                intent.putExtra(ReminderBroadcast.INTENT_FILMITEM_BUNDLE, bundle)
                val pendingIntent = PendingIntent.getBroadcast(
                    app,
                    item.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager?.set(AlarmManager.RTC_WAKEUP, item.watchLaterDate, pendingIntent)
            }
        }
    }

    override fun updateNotificationChannel(item: ChangedFilmItem) {
        if (item.watchLaterDate != -1L && item.watchLaterDate >= Calendar.getInstance().timeInMillis) {
            val intent = Intent(app, ReminderBroadcast::class.java)
            val bundle =
                Bundle().apply { putParcelable(ReminderBroadcast.BUNDLE_FILMITEM, item) }
            intent.putExtra(ReminderBroadcast.INTENT_FILMITEM_BUNDLE, bundle)
            val pendingIntent = PendingIntent.getBroadcast(
                app,
                item.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager?.set(AlarmManager.RTC_WAKEUP, item.watchLaterDate, pendingIntent)
        }
    }
}

interface NotificationUpdater {
    fun updateNotificationChannel(list: List<ChangedFilmItem>)
    fun updateNotificationChannel(item: ChangedFilmItem)
}


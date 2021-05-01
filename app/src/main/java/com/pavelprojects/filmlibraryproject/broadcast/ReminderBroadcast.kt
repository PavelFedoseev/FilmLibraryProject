package com.pavelprojects.filmlibraryproject.broadcast

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.REMINDER_CHANNEL_ID
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity

class ReminderBroadcast : BroadcastReceiver() {
    companion object {
        const val INTENT_FILMITEM_BUNDLE = "intent filmitem"
        const val BUNDLE_FILMITEM = "bundle filmitem"
        const val BUNDLE_OUT = "bundle out"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.getBundleExtra(INTENT_FILMITEM_BUNDLE)

        val filmItem = bundle?.getParcelable<ChangedFilmItem>(BUNDLE_FILMITEM)
        val outIntent = Intent(context, FilmLibraryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        outIntent.putExtra(BUNDLE_OUT, bundle)
        val activityIntent = PendingIntent.getActivity(
            context,
            filmItem?.filmId ?: 1,
            outIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (filmItem?.filmId != null) {
            val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_local_movies_24)
                .setContentTitle("Вы хотели посмотреть фильм: " + filmItem.name)
                .setContentText("Нажмите на уведомление чтобы перейти к описанию")
//                .setLargeIcon(Glide.with(context).asBitmap().load("https://image.tmdb.org/t/p/w92${filmItem.posterPath}").submit().get())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(activityIntent)
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(filmItem.filmId ?: 1, builder.build())
        }

    }

}
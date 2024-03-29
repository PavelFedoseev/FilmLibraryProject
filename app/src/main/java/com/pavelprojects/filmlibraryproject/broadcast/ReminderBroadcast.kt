package com.pavelprojects.filmlibraryproject.broadcast

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pavelprojects.filmlibraryproject.App.Companion.REMINDER_CHANNEL_ID
import com.pavelprojects.filmlibraryproject.R
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
            filmItem?.id ?: 1,
            outIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (filmItem?.id != null) {
            val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_local_movies_24)
                .setContentTitle(context.getString(R.string.reminder_titile) + filmItem.name)
                .setContentText(context.getString(R.string.reminder_text))
//                .setLargeIcon(Glide.with(context).asBitmap().load("https://image.tmdb.org/t/p/w92${filmItem.posterPath}").submit().get())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(activityIntent)
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(filmItem.id, builder.build())
        }

    }

}
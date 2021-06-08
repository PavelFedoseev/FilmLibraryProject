package com.pavelprojects.filmlibraryproject.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity

class NotificationFirebaseService : FirebaseMessagingService() {
    companion object{
        const val TAG = "NotifFirebaseService"
        const val INTENT_FILM_CODE = "filmid"
    }
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val filmid = data["filmid"].toString()
        val intent = Intent(this, FilmLibraryActivity::class.java ).apply {
            putExtra(INTENT_FILM_CODE, filmid)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

}
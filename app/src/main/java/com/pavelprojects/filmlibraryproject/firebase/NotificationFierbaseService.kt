package com.pavelprojects.filmlibraryproject.firebase

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity

class NotificationFirebaseService : FirebaseMessagingService() {
    companion object {
        const val TAG = "NotifFirebaseService"
        const val INTENT_FILM_CODE = "filmid"
        const val DATA_ID_CODE = "filmid"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val filmId = data[DATA_ID_CODE].toString()
        val intent = Intent(this, FilmLibraryActivity::class.java).apply {
            putExtra(INTENT_FILM_CODE, filmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

}
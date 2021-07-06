package com.pavelprojects.filmlibraryproject.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FilmInfoViewModel @Inject constructor(
    var app: Application,
    val repository: FilmRepository,
    val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app) {
    private val filmById = MutableLiveData<FilmItem>()

    fun onArgsReceived(filmId: Int) {
        repository.getFilmById(filmId, object : MaybeObserver<FilmItem> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onSuccess(t: FilmItem) {
                filmById.postValue(t)
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }
        })
    }

    fun onWatchLaterDateAdded(item: ChangedFilmItem) {
        notificationRepository.updateNotificationChannel(item)
    }

    fun observeFilmById(): LiveData<FilmItem> = filmById
}
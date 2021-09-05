package com.pavelprojects.filmlibraryproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class FilmInfoViewModel @Inject constructor(
    var app: Application,
    val repository: FilmRepository,
    private val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app) {
    private val filmById = MutableLiveData<ChangedFilmItem>()
    private var filmId = -1

    companion object {
        private const val TAG = "FilmInfoViewModel"
    }

    fun onArgsReceived(filmId: Int) {
        this.filmId = filmId
        var isChanged = false
        repository.getChangedFilmById(filmId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<ChangedFilmItem> {
                override fun onSubscribe(d: Disposable) {
                    Timber.tag(TAG).d("onSubscribe")
                }

                override fun onSuccess(t: ChangedFilmItem) {
                    isChanged = true
                    filmById.postValue(t)
                    Timber.tag(TAG).d("onSuccess")
                }

                override fun onError(e: Throwable) {
                    Timber.tag(TAG).d(e.toString())
                }

                override fun onComplete() {
                    if(!isChanged){
                        onItemIsNull()
                    }
                    Timber.tag(TAG).d("onComplete")
                }
            }
            )
    }

    fun onWatchLaterDateAdded(item: ChangedFilmItem, timeInMillis: Long) {
        item.watchLaterDate = timeInMillis
        item.isWatchLater = true
        filmById.value = item
        notificationRepository.updateNotificationChannel(item)
    }

    fun observeFilmById(): LiveData<ChangedFilmItem> = filmById

    fun onItemIsNull() {
        repository.getFilmById(filmId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<FilmItem> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onSuccess(t: FilmItem) {
                    filmById.postValue(t.toChangedFilmItem())
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    fun onWatchLaterRemoved(changedFilmItem: ChangedFilmItem) {
        changedFilmItem.isWatchLater = false
        changedFilmItem.watchLaterDate = -1
        filmById.value = changedFilmItem
        notificationRepository.removeNotificationFromChannel(changedFilmItem)
    }
}
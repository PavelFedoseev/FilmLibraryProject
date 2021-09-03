package com.pavelprojects.filmlibraryproject.ui.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toFilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilmInfoViewModel @Inject constructor(
    var app: Application,
    val repository: FilmRepository,
    private val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app) {
    private val filmById = MutableLiveData<ChangedFilmItem>()
    private var filmId = -1

    fun onArgsReceived(filmId: Int) {
        this.filmId = filmId
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

    fun onWatchLaterDateAdded(item: ChangedFilmItem, timeInMillis: Long) {
        item.watchLaterDate = timeInMillis
        item.isWatchLater = true
        filmById.value = item
        notificationRepository.updateNotificationChannel(item)
    }

    fun observeFilmById(): LiveData<ChangedFilmItem> = filmById

    fun onItemIsNull(){
        repository.getChangedFilmById(filmId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<ChangedFilmItem> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: ChangedFilmItem) {
                    filmById.postValue(t)
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
            }
            )
    }
}
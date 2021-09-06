package com.pavelprojects.filmlibraryproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchLaterViewModel @Inject constructor(
    app: Application,
    val repository: FilmRepository,
    val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app) {

    private val listOfWatchLaterFilmItem = MutableLiveData<List<ChangedFilmItem>>()

    fun observeWatchLater(): LiveData<List<ChangedFilmItem>> {
        repository.getWatchLaterFilms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<List<ChangedFilmItem>> {
                override fun onSuccess(list: List<ChangedFilmItem>) {
                    listOfWatchLaterFilmItem.postValue(list)
                    updateNotificationChannel(list)
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })
        return listOfWatchLaterFilmItem
    }

    private fun updateChanged(changedFilmItem: ChangedFilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChanged(changedFilmItem)
        }
    }

    fun onDatePicked(item: ChangedFilmItem) {
        updateChanged(item)
        listOfWatchLaterFilmItem.value?.let { updateNotificationChannel(it) }
    }

    fun getRecyclerSavedPos() = repository.recWatchLaterPos
    fun onRecyclerScrolled(pos: Int) {
        repository.recWatchLaterPos = pos
    }

    private fun updateNotificationChannel(list: List<ChangedFilmItem>) {
        notificationRepository.updateNotificationChannel(list)
    }

    fun onItemSwiped(item: ChangedFilmItem){
        notificationRepository.removeNotificationFromChannel(item)
    }
}
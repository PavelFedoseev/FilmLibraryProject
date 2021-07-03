package com.pavelprojects.filmlibraryproject.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangedViewModel @Inject constructor(app: Application, val repository: FilmRepository) :
    AndroidViewModel(app), NetworkLoadChecker {
    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()
    override val isNetworkLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private val listOfWatchLaterFilmItem = MutableLiveData<List<ChangedFilmItem>>()

    fun observeFavFilms(): LiveData<List<FilmItem>> {
        repository.getFavFilms(object : MaybeObserver<List<FilmItem>> {
            override fun onSuccess(list: List<FilmItem>) {
                listOfFavoriteFilmItem.postValue(list)
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }
        })
        return listOfFavoriteFilmItem
    }

    fun observeWatchLater(): LiveData<List<ChangedFilmItem>> {
        repository.getWatchLaterFilms(object : MaybeObserver<List<ChangedFilmItem>> {
            override fun onSuccess(list: List<ChangedFilmItem>) {
                listOfWatchLaterFilmItem.postValue(list)
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

    fun updateChanged(changedFilmItem: ChangedFilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChanged(changedFilmItem)
        }
    }

    fun getRecyclerSavedPos() = repository.recChangedPos
    fun onRecyclerScrolled(pos: Int) {
        repository.recChangedPos = pos
    }
}
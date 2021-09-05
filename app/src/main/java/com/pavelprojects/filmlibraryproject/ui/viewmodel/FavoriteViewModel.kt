package com.pavelprojects.filmlibraryproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FavoriteViewModel @Inject constructor(
    app: Application,
    val repository: FilmRepository
) : AndroidViewModel(app){

    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()

    fun observeFavFilms(): LiveData<List<FilmItem>> {
        repository.getFavFilms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<List<FilmItem>> {
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

    fun getRecyclerSavedPos() = repository.recWatchLaterPos
    fun onRecyclerScrolled(pos: Int) {
        repository.recFavoritePos = pos
    }
}
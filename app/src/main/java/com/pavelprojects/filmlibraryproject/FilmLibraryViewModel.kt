package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.retrofit.FilmDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilmLibraryViewModel(val app: Application) : AndroidViewModel(app) {
    companion object {
        const val TAG_FLVM = "FilmLibraryViewModel"
    }

    private val repository: FilmRepository = FilmRepository(app)
    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfFilmItem = MutableLiveData<List<FilmItem>>()
    private val snackBarText = MutableLiveData<String>()
    private val isNetworkLoading = MutableLiveData<Boolean>()

    init {
        Log.d(TAG_FLVM, this.toString())
    }


    fun insert(filmItem: FilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(filmItem)
        }
    }

    fun insertAll(list: List<FilmItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(list)
        }
    }

    fun update(filmItem: FilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(filmItem)
        }
    }

    fun getAllFilms(): LiveData<List<FilmItem>> {
        viewModelScope.launch(Dispatchers.IO) {
            listOfFavoriteFilmItem.postValue(repository.getAllFilms())
        }
        return listOfFavoriteFilmItem
    }

    fun getFilmById(id: Long): LiveData<FilmItem?> {
        viewModelScope.launch(Dispatchers.IO) {
            filmItemById.postValue(repository.getFilmById(id))
        }
        return filmItemById
    }

    fun delete(filmItem: FilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(filmItem)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun getPopularMovies(page : Int = 1): LiveData<List<FilmItem>>{
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPopularMovies(page, object : FilmRepository.PopularMoviesResponseListener{
                override fun onSuccess(data: FilmDataResponse?) {
                    listOfFilmItem.postValue(data?.movies)
                }

                override fun onFailure() {
                    snackBarText.postValue(app.resources.getString(R.string.snackbar_network_error))
                }
            })
        }
        return listOfFilmItem
    }

    fun getNetworkLoadingStatus(): LiveData<Boolean> = isNetworkLoading

    fun getSnackBarString(): LiveData<String> = snackBarText
}
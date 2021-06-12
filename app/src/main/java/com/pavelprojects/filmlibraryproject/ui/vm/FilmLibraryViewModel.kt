package com.pavelprojects.filmlibraryproject.ui.vm

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.network.FilmDataResponse
import com.pavelprojects.filmlibraryproject.network.toFilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FilmLibraryViewModel @Inject constructor(var app: Application, val repository: FilmRepository) :
    AndroidViewModel(app) {
    companion object {
        const val TAG = "FilmLibraryViewModel"
        const val LOG_INTERNET = "Network Status"
        const val CODE_FILM_TABLE = 1
        const val CODE_CHANGED_FILM_TABLE = 2
    }

    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()
    private val listOfWatchLaterFilmItem = MutableLiveData<List<ChangedFilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfDatabase = MutableLiveData<List<FilmItem>>()
    private val listOfFilmItem = MutableLiveData<List<FilmItem>>()
    private val snackBarText = MutableLiveData<String>()
    private val isNetworkLoading = MutableLiveData<Boolean>()

    var allPages = 1

    init {
        Log.d(TAG, this.toString())
        if (listOfFavoriteFilmItem.value == null)
            listOfFavoriteFilmItem.postValue(listOf())
    }

    fun insert(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (code == CODE_CHANGED_FILM_TABLE) {
                repository.insert(filmItem, CODE_CHANGED_FILM_TABLE)
            } else
                repository.insert(filmItem, CODE_FILM_TABLE)
        }
    }

    fun insertAll(list: List<FilmItem>, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(list, code)
        }
    }

    fun update(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (code == CODE_FILM_TABLE) {
                repository.update(filmItem, CODE_FILM_TABLE)
            } else {
                repository.update(filmItem, CODE_CHANGED_FILM_TABLE)
            }
        }
    }

    fun updateChanged(changedFilmItem: ChangedFilmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChanged(changedFilmItem)
        }
    }

    fun getAllFilms(): LiveData<List<FilmItem>> {
            //if ((app as App).loadedPage != 1) {
                repository.getAllFilms(object : FilmRepository.FilmListResponseCallback {
                    override fun onSuccess(list: List<FilmItem>) {
                        listOfDatabase.postValue(list)
                    }

                })

            //}
        return listOfDatabase
    }

    fun getFavFilms(): LiveData<List<FilmItem>> {
            repository.getFavFilms(object : FilmRepository.FilmListResponseCallback {
                override fun onSuccess(list: List<FilmItem>) {
                    listOfFavoriteFilmItem.postValue(list)
                }

            })
        return listOfFavoriteFilmItem
    }

    fun getWatchLatter(): LiveData<List<ChangedFilmItem>> {
            repository.getWatchLaterFilms(object : FilmRepository.ChangedFilmListResponseCallback {
                override fun onSuccess(list: List<ChangedFilmItem>) {
                    listOfWatchLaterFilmItem.postValue(list)
                }

            })
        return listOfWatchLaterFilmItem
    }

    fun getFilmById(id: Int): LiveData<FilmItem?> {
            repository.getFilmById(id, object : FilmRepository.FilmResponseCallback{
                override fun onSuccess(filmItem: FilmItem) {
                    filmItemById.postValue(filmItem)
                }
            })
        return filmItemById
    }

    fun delete(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (code == CODE_CHANGED_FILM_TABLE) {
                repository.delete(filmItem, CODE_CHANGED_FILM_TABLE)
            } else
                repository.delete(filmItem, CODE_FILM_TABLE)
        }
    }

    fun deleteAll(code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll(code)
        }
    }

    fun deleteAllIncorrect() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll(CODE_CHANGED_FILM_TABLE)
        }
    }

    fun getPopularMovies(): LiveData<List<FilmItem>> {
        if (isOnline(app))
            initFilmDownloading()
        else
            getCachedFilmList()
        return listOfFilmItem
    }

    fun downloadPopularMovies() {
        if (isOnline(app))
            initFilmDownloading()
        else
            getCachedFilmList()
    }

    fun getLoadingStatus(): Boolean? = isNetworkLoading.value

    fun getCachedFilmList() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllFilms(object : FilmRepository.FilmListResponseCallback {
                override fun onSuccess(list: List<FilmItem>) {
                    listOfDatabase.postValue(list)
                }
            })

        }
    }


    fun initFilmDownloading() {
        Log.d(TAG, "initFilmDownloading: loadedPage = ${(app as? App)?.loadedPage}")
        isNetworkLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPopularMovies(
                (app as? App)?.loadedPage?: 1,
                object : FilmRepository.PopularMoviesResponseListener {
                    override fun onSuccess(data: FilmDataResponse?) {
                        allPages = data?.pages ?: 1
                        if ((app as App).loadedPage == 1) {
                            deleteAll(CODE_FILM_TABLE)
                        }
                        (app as App).loadedPage++
                        if (data != null) {
                            val movies = data.movies.map { it.toFilmItem() }
                            movies.forEach { item ->
                                listOfFavoriteFilmItem.value?.forEach { item1 ->
                                    if (item.id == item1.id) {
                                        item.isLiked = item1.isLiked
                                        item.userComment = item1.userComment
                                    }
                                }
                            }
                            insertAll(movies, CODE_FILM_TABLE)
                            listOfFilmItem.postValue(movies)
                        }
                        isNetworkLoading.postValue(false)
                    }

                    override fun onFailure() {
                        snackBarText.postValue(app.resources.getString(R.string.snackbar_download_error))
                        isNetworkLoading.postValue(false)
                        getCachedFilmList()
                    }
                })
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        Log.i(LOG_INTERNET, "Connection failed")
        return false
    }


    fun getNetworkLoadingStatus(): LiveData<Boolean> = isNetworkLoading

    fun getSnackBarString(): LiveData<String> = snackBarText
}
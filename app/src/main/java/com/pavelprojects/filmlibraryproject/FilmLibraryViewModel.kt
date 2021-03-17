package com.pavelprojects.filmlibraryproject

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
        const val LOG_INTERNET = "Network Status"
        const val CODE_FILM_DB = 1
        const val CODE_FAV_FILM_DB = 2
    }

    private val repository: FilmRepository = App.instance.repository
    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfFilmItem = MutableLiveData<List<FilmItem>>()
    private val snackBarText = MutableLiveData<String>()
    private val isNetworkLoading = MutableLiveData<Boolean>()
    var page: Int = 1

    var allPages = 1

    init {
        Log.d(TAG_FLVM, this.toString())
    }

    fun insert(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(filmItem, code)
        }
    }

    fun insertAll(list: List<FilmItem>, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAll(list, code)
        }
    }

    fun update(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(filmItem, code)
        }
    }

    fun getAllFilms(code: Int): LiveData<List<FilmItem>> {
        viewModelScope.launch(Dispatchers.IO) {
            listOfFavoriteFilmItem.postValue(repository.getAllFilms(code))
        }
        return if (code == CODE_FILM_DB) listOfFilmItem
        else listOfFavoriteFilmItem
    }

    fun getFilmById(id: Long, code: Int): LiveData<FilmItem?> {
        viewModelScope.launch(Dispatchers.IO) {
            filmItemById.postValue(repository.getFilmById(id, code))
        }
        return filmItemById
    }

    fun delete(filmItem: FilmItem, code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(filmItem, code)
        }
    }

    fun deleteAll(code: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll(code)
        }
    }

    fun getPopularMovies(page: Int = 1): LiveData<List<FilmItem>> {
        if (isOnline(app))
            initFilmDownloading(page)
        return listOfFilmItem
    }

    fun downloadPopularMovies(page: Int = 1) {
        if (isOnline(app))
            initFilmDownloading()
    }

    fun getLoadingStatus(): Boolean? = isNetworkLoading.value


    fun initFilmDownloading(page: Int = 1) {
        isNetworkLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPopularMovies(
                page,
                object : FilmRepository.PopularMoviesResponseListener {
                    override fun onSuccess(data: FilmDataResponse?) {
                        allPages = data?.pages ?: 1
                        listOfFilmItem.postValue(data?.movies)
                        this@FilmLibraryViewModel.page = data?.page ?: 1
                        isNetworkLoading.postValue(false)
                    }

                    override fun onFailure() {
                        snackBarText.postValue(app.resources.getString(R.string.snackbar_network_error))
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
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
    }

    private val repository: FilmRepository = FilmRepository(app)
    private val listOfFavoriteFilmItem = MutableLiveData<List<FilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfFilmItem = MutableLiveData<List<FilmItem>>()
    private val snackBarText = MutableLiveData<String>()
    private val isNetworkLoading = MutableLiveData<Boolean>()
    private var currentPage = MutableLiveData<Int>()

    var allPages = 1

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
        if(isOnline(app))
        initFilmDownloading(page)
        return listOfFilmItem
    }

    fun getLoadingStatus(): Boolean? = isNetworkLoading.value

    fun getCurrentPageLD(): LiveData<Int> = currentPage

    fun getCurrentPage(): Int? = currentPage.value

    fun initFilmDownloading(page : Int = 1){
        isNetworkLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPopularMovies(page, object : FilmRepository.PopularMoviesResponseListener{
                override fun onSuccess(data: FilmDataResponse?) {
                    allPages = data?.pages ?: 1
                    listOfFilmItem.postValue(data?.movies)
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
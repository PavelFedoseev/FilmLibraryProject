package com.pavelprojects.filmlibraryproject.ui.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.repository.NotificationRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class FilmLibraryViewModel @Inject constructor(
    var app: Application,
    val repository: FilmRepository,
    private val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app), NetworkLoadChecker {
    companion object {
        const val TAG = "FilmLibraryViewModel"
        const val LOG_INTERNET = "Network Status"
        const val CODE_FILM_TABLE = 1
        const val CODE_CHANGED_FILM_TABLE = 2
    }

    private val listOfChangedFilmItem = MutableLiveData<List<ChangedFilmItem>>()
    private val listOfWatchLaterFilmItem = MutableLiveData<List<ChangedFilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfDatabase = MutableLiveData<List<FilmItem>>()
    private val listOfDownloads = MutableLiveData<List<FilmItem>>()
    private val snackBarText = MutableLiveData<String>()
    override val isNetworkLoading = MutableLiveData(true)

    private var _pagingFlowable: MutableLiveData<Flowable<PagingData<FilmItem>>?> = MutableLiveData<Flowable<PagingData<FilmItem>>?>(null)

    private val _isConnectionStatus = MutableLiveData(true)
    val isConnectionStatus: LiveData<Boolean> = _isConnectionStatus


    var allPages = 1

    init {
        Log.d(TAG, this.toString())
        if (listOfChangedFilmItem.value == null)
            listOfChangedFilmItem.postValue(listOf())
    }

    fun insert(filmItem: FilmItem, code: Int) {
        Completable.fromRunnable {
            if (code == CODE_CHANGED_FILM_TABLE) {
                repository.insert(filmItem, CODE_CHANGED_FILM_TABLE)
            } else
                repository.insert(filmItem, CODE_FILM_TABLE)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun insertChanged(changedFilmItem: ChangedFilmItem){
        Completable.fromRunnable {
                repository.insertChanged(changedFilmItem)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun insertAll(list: List<FilmItem>, code: Int) {
        Completable.fromRunnable {
            repository.insertAll(list, code)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun update(filmItem: FilmItem, code: Int) {
        Completable.fromRunnable {
            if (code == CODE_FILM_TABLE) {
                repository.update(filmItem, CODE_FILM_TABLE)
            } else {
                repository.update(filmItem, CODE_CHANGED_FILM_TABLE)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun updateChanged(changedFilmItem: ChangedFilmItem) {
        Completable.fromRunnable {
            repository.updateChanged(changedFilmItem)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }


    fun observeAllChanged(): LiveData<List<ChangedFilmItem>> {
        repository.getAllChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<List<ChangedFilmItem>> {
                override fun onSuccess(list: List<ChangedFilmItem>) {
                    listOfChangedFilmItem.postValue(list)
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })
        return listOfChangedFilmItem
    }

    fun observeNotificationList(): LiveData<List<ChangedFilmItem>> {
        repository.getWatchLaterFilms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<List<ChangedFilmItem>> {
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

    fun observeNetworkLoadingStatus(): LiveData<Boolean> = isNetworkLoading

    fun observeSnackBarString(): LiveData<String> = snackBarText

    fun getFilmById(id: Int): LiveData<FilmItem?> {
        repository.getFilmById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<FilmItem> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onSuccess(t: FilmItem) {
                    filmItemById.postValue(t)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })
        return filmItemById
    }

    fun delete(filmItem: FilmItem, code: Int) {
        Completable.fromRunnable {
            if (code == CODE_CHANGED_FILM_TABLE) {
                repository.delete(filmItem, CODE_CHANGED_FILM_TABLE)
            } else
                repository.delete(filmItem, CODE_FILM_TABLE)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun deleteAll(code: Int) {
        Completable.fromRunnable {
            repository.deleteAll(code)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }


    @ExperimentalCoroutinesApi
    fun getPopularFilms(): LiveData<Flowable<PagingData<FilmItem>>?> {
        return _pagingFlowable
    }

    @ExperimentalCoroutinesApi
    fun onInitRemoteSource(isReload: Boolean){
        if(_pagingFlowable.value == null || isReload){
            _pagingFlowable.postValue(repository.getRemoteMovies().cachedIn(viewModelScope))
        }
        else
        _pagingFlowable.postValue(_pagingFlowable.value)
    }


    fun getCachedFilmList() {
        repository.getAllFilms().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MaybeObserver<List<FilmItem>> {
                override fun onSuccess(list: List<FilmItem>) {
                    listOfDatabase.postValue(list)
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }
            })

    }


    fun subscribeToDatabase(): LiveData<List<FilmItem>> {
        getCachedFilmList()
        return listOfDatabase
    }
    fun subscribeToDownloads() = listOfDownloads


//    fun initModelDownloads() {
//        if (isOnline(app))
//            initFilmDownloading()
//        else
//            getCachedFilmList()
//    }

//    private fun isOnline(context: Context): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val capabilities =
//            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//        if (capabilities != null) {
//            when {
//                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
//                    Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_CELLULAR")
//                    return true
//                }
//                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
//                    Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_WIFI")
//                    return true
//                }
//                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
//                    Log.i(LOG_INTERNET, "NetworkCapabilities.TRANSPORT_ETHERNET")
//                    return true
//                }
//            }
//        }
//        Log.i(LOG_INTERNET, "Connection failed")
//        return false
//    }

//    fun initFilmDownloading() {
//        Log.d(TAG, "initFilmDownloading: loadedPage = ${repository.loadedPage}")
//        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            app.applicationContext.resources.configuration.locales[0].language
//        } else {
//            app.applicationContext.resources.configuration.locale.language
//        }
//        repository.getRemoteMovies(
//            repository.loadedPage,
//            languageCode
//        )
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                object : SingleObserver<FilmDataResponse> {
//                    override fun onSuccess(t: FilmDataResponse) {
//                        allPages = t.totalPages
//                        if (repository.loadedPage == 1) {
//                            deleteAll(CODE_FILM_TABLE)
//                        }
//                        repository.loadedPage++
//                        val movies = t.films.map { it.toFilmItem() }
//                        movies.forEach { item ->
//                            listOfChangedFilmItem.value?.forEach { item1 ->
//                                if (item.id == item1.id) {
//                                    item.isLiked = item1.isLiked
//                                    item.userComment = item1.userComment
//                                    item.isWatchLater = item1.isWatchLater
//                                }
//                            }
//                            item.posterPath = item.posterPath?.let { repository.toImageUrl(it) }
//                            item.backdropPath = item.backdropPath?.let { repository.toImageUrl(it) }
//                        }
//                        insertAll(movies, CODE_FILM_TABLE)
//                        listOfDownloads.postValue(movies)
//                        isNetworkLoading.postValue(true)
//                    }
//
//                    override fun onSubscribe(d: Disposable) {
//                    }
//
//                    override fun onError(e: Throwable) {
//                        snackBarText.postValue(app.resources.getString(R.string.snackbar_download_error))
//                        isNetworkLoading.postValue(false)
//                        getCachedFilmList()
//                    }
//                })
//    }

    fun getRecyclerSavedPos() = repository.recFilmListPos
    fun onRecyclerScrolled(pastVisibleItem: Int) {
        repository.recFilmListPos = pastVisibleItem
    }

    fun getLoadedPage() = repository.getLoadedPage()

    fun onRateButtonClicked(item: FilmItem, changedFilmItem: ChangedFilmItem) {
        update(item, CODE_FILM_TABLE)
        update(item, CODE_CHANGED_FILM_TABLE)
        if (item.isLiked || item.isWatchLater) {
            insert(item, CODE_CHANGED_FILM_TABLE)
        } else {
            delete(item, CODE_CHANGED_FILM_TABLE)
        }
        if (changedFilmItem.watchLaterDate != -1L) {
            insertChanged(changedFilmItem)
            updateChanged(changedFilmItem)
            notificationRepository.updateNotificationChannel(changedFilmItem)
        }
    }

    fun onOnlineStatusChanged(isOnline: Boolean) {
        _isConnectionStatus.postValue(isOnline)
    }

    fun onObserversInitialized() {
//        initModelDownloads()
    }
}
package com.pavelprojects.filmlibraryproject.ui.viewmodel

import android.app.Application
import android.os.Build
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
import timber.log.Timber
import javax.inject.Inject

class FilmLibraryViewModel @Inject constructor(
    var app: Application,
    val repository: FilmRepository,
    private val notificationRepository: NotificationRepository
) :
    AndroidViewModel(app) {
    companion object {
        const val TAG = "FilmLibraryViewModel"
        const val CODE_FILM_TABLE = 1
        const val CODE_CHANGED_FILM_TABLE = 2
    }

    private val listOfChangedFilmItem = MutableLiveData<List<ChangedFilmItem>>()
    private val listOfWatchLaterFilmItem = MutableLiveData<List<ChangedFilmItem>>()
    private val filmItemById = MutableLiveData<FilmItem?>()
    private val listOfDatabase = MutableLiveData<List<FilmItem>>()

    private var _pagingFlowable: MutableLiveData<Flowable<PagingData<FilmItem>>?> =
        MutableLiveData<Flowable<PagingData<FilmItem>>?>(null)

    private val _isConnectionOk = MutableLiveData(true)
    val isConnectionStatus: LiveData<Boolean> = _isConnectionOk

    private val _isSearchMode = MutableLiveData(false)
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private val _searchQuery = MutableLiveData<String?>(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private val _filmSource = MutableLiveData<FilmSource?>(null)
    val filmSource: LiveData<FilmSource?> = _filmSource

    init {
        Timber.tag(TAG).d(this.toString())
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

    fun insertChanged(changedFilmItem: ChangedFilmItem) {
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

    @ExperimentalCoroutinesApi
    fun onFragmentCreated() {
        when (_filmSource.value) {
            FilmSource.SEARCH -> {
                _searchQuery.value?.let {
                    initSearchSource(it)
                } ?: initRemoteSource(true)
            }
            FilmSource.REMOTE -> {
                initRemoteSource(false)
            }
            FilmSource.LOCAL -> {
                requestCachedFilmList()
            }
            else -> {
                initRemoteSource(true)
            }
        }
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

    @ExperimentalCoroutinesApi
    fun getFilmSourceFlowable(): LiveData<Flowable<PagingData<FilmItem>>?> {
        return _pagingFlowable
    }

    @ExperimentalCoroutinesApi
    fun initRemoteSource(isReload: Boolean) {
        if (_pagingFlowable.value == null || isReload) {
            _pagingFlowable.postValue(repository.getRemoteMovies().cachedIn(viewModelScope))
        } else
            _pagingFlowable.postValue(_pagingFlowable.value)
        _filmSource.postValue(FilmSource.REMOTE)
    }

    @ExperimentalCoroutinesApi
    fun initSearchSource(searchQuery: String, isReload: Boolean = false) {
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            app.applicationContext.resources.configuration.locales[0].language
        } else {
            app.applicationContext.resources.configuration.locale.language
        }
        if (searchQuery != _searchQuery.value || isReload) {
            _pagingFlowable.postValue(repository.getMovieBySearch(searchQuery, languageCode))
        }
        _filmSource.postValue(FilmSource.SEARCH)
    }

    @ExperimentalCoroutinesApi
    fun onSearchBarButtonClicked(text: String) {
        if (_isSearchMode.value == false) {
            _isSearchMode.postValue(true)

            val searchQuery = text.replace(' ', '+', true)
            initSearchSource(searchQuery)
            _searchQuery.postValue(searchQuery)
            _filmSource.postValue(FilmSource.SEARCH)
        } else {
            _isSearchMode.postValue(false)
            _searchQuery.postValue(null)
            if (isConnectionStatus.value == true) {
                initRemoteSource(true)
            } else {
                requestCachedFilmList()
                _filmSource.postValue(FilmSource.LOCAL)
            }
        }
    }

    fun onEditTextSearchChanged() {
        if (_isSearchMode.value == true) {
            _isSearchMode.postValue(false)
        }

    }

    fun requestCachedFilmList() {
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
        requestCachedFilmList()
        return listOfDatabase
    }

    fun getRecyclerSavedPos() = repository.recFilmListPos
    fun onRecyclerScrolled(pastVisibleItem: Int) {
        repository.recFilmListPos = pastVisibleItem
    }

    fun getInitState() = repository.getInitState()

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
        _isConnectionOk.postValue(isOnline)
        if (isOnline) {
            initRemoteSource(_filmSource.value != FilmSource.REMOTE)
        } else
            if (getInitState()) {
                requestCachedFilmList()
                _filmSource.postValue(FilmSource.LOCAL)
            }
    }

    fun onRefreshOccurred() {
        if (_isConnectionOk.value == true) {
            if (_filmSource.value == FilmSource.SEARCH) {
                _searchQuery.value?.let { initSearchSource(it, isReload = true) }
            } else
                initRemoteSource(true)
        }
    }
}

enum class FilmSource {
    REMOTE,
    SEARCH,
    LOCAL
}
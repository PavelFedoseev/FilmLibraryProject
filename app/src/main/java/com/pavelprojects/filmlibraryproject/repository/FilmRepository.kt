package com.pavelprojects.filmlibraryproject.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.network.FilmDataPagingSource
import com.pavelprojects.filmlibraryproject.network.RetroApi
import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class FilmRepository {

    companion object {
        const val TAG = "FilmRepository"
        const val CODE_FILM_TABLE = 1
    }

    var recFilmListPos: Int = 0
    var recWatchLaterPos: Int = 0
    var recFavoritePos: Int = 0

    init {
        App.appComponent.inject(this)
    }

    @Inject
    lateinit var filmDatabase: FilmDatabase

    @Inject
    lateinit var retroApi: RetroApi

    @Inject
    lateinit var pagingSource: FilmDataPagingSource

    val filmItemDao = filmDatabase.getFilmItemDao()
    val changedItemDao = filmDatabase.getChangedItemDao()


    fun insert(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.insert(filmItem)
        else changedItemDao.insert(filmItem.toChangedFilmItem())
    }

    fun insertChanged(changedFilmItem: ChangedFilmItem) {
        changedItemDao.insert(changedFilmItem)
    }

    fun updateChanged(changedFilmItem: ChangedFilmItem) {
        changedItemDao.update(changedFilmItem)
    }

    fun insertAll(listOfFilms: List<FilmItem>, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.insertAll(listOfFilms)
        else changedItemDao.insertAllFav(listOfFilms.map { it.toChangedFilmItem() })
    }

    fun update(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.update(filmItem)
        else changedItemDao.update(filmItem.toChangedFilmItem())
    }

    fun getAllFilms() = filmItemDao.getAll()

    fun getFavFilms() = changedItemDao.getAllFav()

    fun getWatchLaterFilms() = changedItemDao.getAllWatchLater()

    fun getAllChanged() = changedItemDao.getAllChanged()

    fun getFilmById(id: Int) = filmItemDao.getById(id)

    fun getChangedFilmById(id: Int) = changedItemDao.getById(id)

    fun delete(filmItem: FilmItem, code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.delete(filmItem)
        else changedItemDao.delete(filmItem.toChangedFilmItem())
    }

    fun deleteAll(code: Int) {
        if (code == CODE_FILM_TABLE)
            filmItemDao.clearFilms()
        else changedItemDao.deleteAllChangedFilms()
    }

    @ExperimentalCoroutinesApi
    fun getMovieBySearch(query: String, language: String): Flowable<PagingData<FilmItem>> {
        pagingSource.searchQuery = query
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 30,
                prefetchDistance = 5,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                pagingSource
            }
        ).flowable
    }


    @ExperimentalCoroutinesApi
    fun getRemoteMovies(): Flowable<PagingData<FilmItem>> {
        pagingSource.searchQuery = null
        return Pager(
            config = FilmDataPagingSource.PAGING_CONFIG,
            pagingSourceFactory = { pagingSource }
        ).flowable
    }

    fun reloadSource(){
        pagingSource.invalidate()
    }

    fun getInitState() = pagingSource.isFirstInit
}
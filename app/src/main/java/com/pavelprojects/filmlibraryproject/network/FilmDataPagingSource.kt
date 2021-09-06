package com.pavelprojects.filmlibraryproject.network

import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FilmDataPagingSource private constructor(
    val filmDatabase: FilmDatabase,
    val retroApi: RetroApi,
    var language: String,
    private var sortBy: String = RetroApi.FILTER_TMDB_POPUlAR,
    var searchQuery: String? = null
) : RxPagingSource<Int, FilmItem>() {

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            maxSize = 30,
            prefetchDistance = 5,
            initialLoadSize = 40
        )

        fun createSource(
            filmDatabase: FilmDatabase,
            retroApi: RetroApi,
            language: String,
            sortBy: String = RetroApi.FILTER_TMDB_POPUlAR
        ): FilmDataPagingSource {
            return FilmDataPagingSource(filmDatabase, retroApi, language, sortBy)
        }

        fun createSearchSource(
            filmDatabase: FilmDatabase,
            retroApi: RetroApi,
            language: String,
            searchQuery: String
        ): FilmDataPagingSource {
            return FilmDataPagingSource(
                retroApi = retroApi,
                language = language,
                searchQuery = searchQuery,
                filmDatabase = filmDatabase
            )
        }
    }

    var loadedPage: Int = 1
    var filmList = emptyList<FilmItem>()
    var isFirstInit = true

    override fun getRefreshKey(state: PagingState<Int, FilmItem>): Int? {
        return state.anchorPosition
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FilmItem>> {
        val page = params.key ?: 1
        val pageSize = params.loadSize.coerceAtMost(RetroApi.MAX_PAGE_SIZE)
        val response = if (searchQuery == null) retroApi.getPopularMovies(
            RetroApi.API_KEY_TMDB,
            sortBy,
            language,
            page
        ) else retroApi.provideMovieSearch(RetroApi.API_KEY_TMDB, searchQuery, language, page)
        return response
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, page) }
            .doAfterSuccess {
                loadedPage = page
                filmDatabase.runInTransaction {
                    if (page == 1 && isFirstInit) {
                        filmDatabase.getFilmItemDao().clearFilms()
                    }
                    filmDatabase.getFilmItemDao().insertAll(filmList)
                }
                isFirstInit = false
            }
            .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(data: FilmDataResponse, page: Int): LoadResult<Int, FilmItem> {
        filmList = data.films.map { it.toFilmItem() }
        return LoadResult.Page(
            data = filmList,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (page == data.totalPages) null else page + 1
        )
    }
}
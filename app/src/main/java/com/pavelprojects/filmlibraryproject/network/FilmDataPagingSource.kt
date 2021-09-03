package com.pavelprojects.filmlibraryproject.network

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FilmDataPagingSource (
    val filmDatabase: FilmDatabase,
    val retroApi: RetroApi,
    var language: String,
    private var sortBy: String = RetroApi.FILTER_TMDB_POPUlAR,
    var searchQuery: String? = null
) : RxPagingSource<Int, FilmItem>() {
    var loadedPage: Int = 1
    var filmList = emptyList<FilmItem>()

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
            .map {toLoadResult(it, page)}
            .doAfterSuccess{
                loadedPage = page
                filmDatabase.runInTransaction {
                    if(page == 1){
                        filmDatabase.getFilmItemDao().clearFilms()
                    }
                    filmDatabase.getFilmItemDao().insertAll(filmList)
                }
            }
            .onErrorReturn{ LoadResult.Error(it)}
    }

    private fun toLoadResult(data: FilmDataResponse, page: Int): LoadResult<Int, FilmItem> {
        filmList = data.films.map{ it.toFilmItem() }
        return LoadResult.Page(
            data = filmList,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (page == data.totalPages) null else page + 1
        )
    }
}
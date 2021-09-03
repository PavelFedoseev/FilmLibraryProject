package com.pavelprojects.filmlibraryproject.network
//
//import androidx.paging.ExperimentalPagingApi
//import androidx.paging.LoadType
//import androidx.paging.PagingState
//import androidx.paging.rxjava2.RxRemoteMediator
//import com.pavelprojects.filmlibraryproject.database.FilmDatabase
//import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
//import com.pavelprojects.filmlibraryproject.database.entity.FilmItemRemoteKeys
//import io.reactivex.Single
//import io.reactivex.schedulers.Schedulers
//import java.io.InvalidObjectException
//
//@ExperimentalPagingApi
//class FilmDataMediator(
//    val retroApi: RetroApi, val filmDatabase: FilmDatabase,
//    private val apiKey: String,
//    private val locale: String,
//    private var sortBy: String = RetroApi.FILTER_TMDB_POPUlAR
//) : RxRemoteMediator<Int, FilmItem>() {
//
//    companion object {
//        const val INVALID_PAGE = -1
//    }
//
//    override fun loadSingle(
//        loadType: LoadType,
//        state: PagingState<Int, FilmItem>
//    ): Single<MediatorResult> {
//        return Single.just(loadType)
//            .subscribeOn(Schedulers.io())
//            .map {
//                when (it) {
//                    LoadType.REFRESH -> {
//                        val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
//                        remoteKeys?.nextKey?.minus(1) ?: 1
//                    }
//                    LoadType.PREPEND -> {
//                        val remoteKeys = getRemoteKeyForFirstItem(state)
//                            ?: throw InvalidObjectException("Result is empty")
//                        remoteKeys.prevKey ?: INVALID_PAGE
//                    }
//                    LoadType.APPEND -> {
//                        val remoteKeys = getRemoteKeyForLastItem(state)
//                            ?: throw InvalidObjectException("Result is empty")
//                        remoteKeys.nextKey ?: INVALID_PAGE
//                    }
//                }
//            }.flatMap { page ->
//                if (page == INVALID_PAGE) {
//                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
//                } else {
//                    retroApi.getPopularMovies(apiKey, sortBy, locale, page)
//                        .subscribeOn(Schedulers.io()).map {
//                        insertToDatabase(page, loadType, it)
//                    }
//                        .map<MediatorResult> { MediatorResult.Success(endOfPaginationReached = it.totalPages == it.page) }
//                        .onErrorReturn { MediatorResult.Error(it) }
//                }
//            }
//    }
//
//    private fun insertToDatabase(
//        page: Int,
//        loadType: LoadType,
//        data: FilmDataResponse
//    ): FilmDataResponse {
//        filmDatabase.runInTransaction {
//            if (loadType == LoadType.REFRESH) {
//                filmDatabase.getFilmItemKeysDao().clearRemoteKeys()
//                filmDatabase.getFilmItemDao().clearFilms()
//            }
//            val prevKey = if (page == 1) null else page - 1
//            val nextKey = if (data.totalPages == data.page) null else page + 1
//            val keys = data.films.map {
//                FilmItemRemoteKeys(it.id, prevKey, nextKey)
//            }
//            filmDatabase.getFilmItemKeysDao().insertAll(keys)
//            filmDatabase.getFilmItemDao().insertAll(data.films.map { it.toFilmItem() })
//        }
//        return data
//    }
//
//    private fun getRemoteKeyForLastItem(state: PagingState<Int, FilmItem>): FilmItemRemoteKeys? =
//        state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { filmItem ->
//            filmDatabase.getFilmItemKeysDao().remoteKeysByMovieId(filmItem.id)
//        }
//
//    private fun getRemoteKeyForFirstItem(state: PagingState<Int, FilmItem>): FilmItemRemoteKeys? =
//        state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { filmItem ->
//            filmDatabase.getFilmItemKeysDao().remoteKeysByMovieId(filmItem.id)
//        }
//
//    private fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, FilmItem>): FilmItemRemoteKeys? {
//        return state.anchorPosition?.let { position ->
//            state.closestItemToPosition(position)?.id?.let { id ->
//                filmDatabase.getFilmItemKeysDao().remoteKeysByMovieId(id)
//            }
//        }
//    }
//
//}
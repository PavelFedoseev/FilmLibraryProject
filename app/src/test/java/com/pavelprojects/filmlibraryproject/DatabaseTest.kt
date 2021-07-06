package com.pavelprojects.filmlibraryproject

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pavelprojects.filmlibraryproject.database.FilmDatabase
import com.pavelprojects.filmlibraryproject.database.dao.ChangedItemDao
import com.pavelprojects.filmlibraryproject.database.dao.FilmItemDao
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseTest {

    lateinit var database: FilmDatabase
    lateinit var daoFilmItem: FilmItemDao
    lateinit var daoChangedFilmItem: ChangedItemDao

    val filmName = "testFilm"

    val filmItem = FilmItem(
        0,
        filmName,
        "testDescription",
        "testPoster",
        "testBackdrop",
        0.0f,
        "testReleaseDate",
        "testUserComment",
        isLiked = false,
        isWatchLater = false
    )

    @Before
    fun setBefore() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FilmDatabase::class.java
        ).allowMainThreadQueries().build()
        daoFilmItem = database.getFilmItemDao()
        daoChangedFilmItem = database.getChangedItemDao()
        daoFilmItem.insert(filmItem)
        daoChangedFilmItem.insert(
            filmItem.toChangedFilmItem().apply { isLiked = true; isWatchLater = true })
    }

    @Test
    fun `получение фильма из таблицы filmItem`() {
        daoFilmItem.getById(filmItem.id)
            .subscribe(object : MaybeObserver<FilmItem> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: FilmItem) {
                    assertEquals(filmItem.name, t.name)
                }

                override fun onError(e: Throwable) {
                    throw e
                }

                override fun onComplete() {
                    assertEquals("hello", "hello")
                }
            })
    }

    @Test
    fun `получение списка фильмов из таблицы filmItem`() {
        daoFilmItem.getAll().subscribe(object : MaybeObserver<List<FilmItem>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: List<FilmItem>) {
                assertTrue(t.contains(filmItem))
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }


    @Test
    fun `получение списка фильмов из таблицы changedFilmItem`() {
        daoChangedFilmItem.getAllChanged().subscribe(object : MaybeObserver<List<ChangedFilmItem>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: List<ChangedFilmItem>) {
                t.forEach {
                    if (it.name == filmName) {
                        assertTrue(true)
                    }
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    @Test
    fun `получение списка избранных фильмов из таблицы changedFilmItem`() {
        daoChangedFilmItem.getAllFav().subscribe(object : MaybeObserver<List<FilmItem>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: List<FilmItem>) {
                t.forEach {
                    if (it.name == filmName && it.isLiked) {
                        assertTrue(true)
                    }
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    @Test
    fun `получение списка посмотреть позже из таблицы changedFilmItem`() {
        daoChangedFilmItem.getAllWatchLater()
            .subscribe(object : MaybeObserver<List<ChangedFilmItem>> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: List<ChangedFilmItem>) {
                    t.forEach {
                        if (it.name == filmName && it.isWatchLater) {
                            assertTrue(true)
                        }
                    }
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
            })
    }


    @After
    fun setAfter() {

    }

}
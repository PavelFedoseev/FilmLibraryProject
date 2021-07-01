package com.pavelprojects.filmlibraryproject

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class IntegrationTest {

    @Mock
    lateinit var repository: FilmRepository

    lateinit var filmLibViewModel: FilmLibraryViewModel

    @Before
    fun setBefore() {
        MockitoAnnotations.initMocks(this)
        filmLibViewModel =
            FilmLibraryViewModel(ApplicationProvider.getApplicationContext(), repository)
    }

    @Test
    fun `проверка работы ViewModel на получение данных из репозитория`() {


        var isGetFilmsChecked = false
        var isGetFavFilmsChecked = false
        var isGetWatchLaterChecked = false
        var isGetFilmByIdChecked = false

        Mockito.`when`(repository.getAllFilms(any(FilmRepository.FilmListResponseCallback::class.java)))
            .then {
                isGetFilmsChecked = true
                Any()
            }
        Mockito.`when`(repository.getAllChanged(any(FilmRepository.ChangedFilmListResponseCallback::class.java)))
            .then {
                isGetFavFilmsChecked = true
                Any()
            }
        Mockito.`when`(repository.getWatchLaterFilms(any(FilmRepository.ChangedFilmListResponseCallback::class.java)))
            .then {
                isGetWatchLaterChecked = true
                Any()
            }
        Mockito.`when`(
            repository.getFilmById(
                Mockito.anyInt(),
                any(FilmRepository.FilmResponseCallback::class.java)
            )
        ).then {
            isGetFilmByIdChecked = true
            Any()
        }

        filmLibViewModel.subscribeToDatabase()
        filmLibViewModel.getAllChanged()
        filmLibViewModel.getNotificationList()
        filmLibViewModel.getFilmById(0)
        filmLibViewModel.initFilmDownloading()

        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(isGetFilmsChecked)
        assertTrue(isGetFavFilmsChecked)
        assertTrue(isGetWatchLaterChecked)
        assertTrue(isGetFilmByIdChecked)
    }

    @Test
    fun `проверка работы ViewModel на запись данных в бд`() {


        val filmItem = FilmItem(
            0,
            "testFilmName",
            "testDescription",
            "testPoster",
            "testBackdrop",
            0.0f,
            "testReleaseDate",
            "testUserComment",
            isLiked = false,
            isWatchLater = false
        )

        var isInsertChecked = false
        var isUpdateChecked = false
        var isDeleteChecked = false

        Mockito.`when`(
            repository.insert(
                Mockito.any(FilmItem::class.java) ?: filmItem,
                Mockito.anyInt()
            )
        ).then {
            isInsertChecked = true
            Any()
        }
        Mockito.`when`(
            repository.update(
                Mockito.any(FilmItem::class.java) ?: filmItem,
                Mockito.anyInt()
            )
        ).then {
            isUpdateChecked = true
            Any()
        }
        Mockito.`when`(
            repository.delete(
                Mockito.any(FilmItem::class.java) ?: filmItem,
                Mockito.anyInt()
            )
        ).then {
            isDeleteChecked = true
            Any()
        }
        runBlocking(Dispatchers.IO) {
            filmLibViewModel.insert(filmItem, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        }
        runBlocking(Dispatchers.IO) {
            filmLibViewModel.update(filmItem, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        }
        runBlocking(Dispatchers.IO) {
            filmLibViewModel.delete(filmItem, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        }


        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(isInsertChecked)
        assertTrue(isUpdateChecked)
        assertTrue(isDeleteChecked)
    }

    @Test
    fun `проверка работы ViewModel на загрузку фильмов`() {


        var isDownloadChecked = false
        Mockito.`when`(
            repository.getPopularMovies(
                Mockito.anyInt(),
                any(FilmRepository.PopularMoviesResponseListener::class.java)
            )
        ).then {
            isDownloadChecked = true
            Any()
        }

        runBlocking(Dispatchers.IO) {
            filmLibViewModel.initFilmDownloading()
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(isDownloadChecked)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
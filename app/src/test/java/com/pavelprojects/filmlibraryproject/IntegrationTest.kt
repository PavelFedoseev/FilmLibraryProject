package com.pavelprojects.filmlibraryproject

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.repository.FilmRepository
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
import junit.framework.Assert.assertTrue
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
    fun setBefore(){
        MockitoAnnotations.initMocks(this)
        filmLibViewModel = FilmLibraryViewModel(ApplicationProvider.getApplicationContext(), repository)
    }
    @Test
    fun `проверка работы ViewModel на получение данных из репозитория`(){
        shadowOf(Looper.getMainLooper()).idle()

        var isGetFilmsChecked = false
        var isGetFavFilmsChecked = false
        var isGetWatchLaterChecked = false
        var isGetFilmByIdChecked = false

        Mockito.`when`(repository.getAllFilms(any(FilmRepository.FilmListResponseCallback::class.java))).then {
            isGetFilmsChecked = true
            Any()
        }
        Mockito.`when`(repository.getFavFilms(any(FilmRepository.FilmListResponseCallback::class.java))).then {
            isGetFavFilmsChecked = true
            Any()
        }
        Mockito.`when`(repository.getWatchLaterFilms(any(FilmRepository.ChangedFilmListResponseCallback::class.java))).then {
            isGetWatchLaterChecked = true
            Any()
        }
        Mockito.`when`(repository.getFilmById(Mockito.anyInt(), any(FilmRepository.FilmResponseCallback::class.java))).then {
            isGetFilmByIdChecked = true
            Any()
        }

        filmLibViewModel.getAllFilms()
        filmLibViewModel.getFavFilms()
        filmLibViewModel.getWatchLatter()
        filmLibViewModel.getFilmById(0)
        filmLibViewModel.initFilmDownloading()

        assertTrue(isGetFilmsChecked)
        assertTrue(isGetFavFilmsChecked)
        assertTrue(isGetWatchLaterChecked)
        assertTrue(isGetFilmByIdChecked)
    }
    @Test
    fun `проверка работы ViewModel на запись данных в бд`(){
        shadowOf(Looper.getMainLooper()).idle()

        var isInsertChecked = false
        var isUpdateChecked = false
        var isDeleteChecked = false

        Mockito.`when`(repository.insert(any(FilmItem::class.java),  FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)).then {
            isInsertChecked = true
            Any()
        }
        Mockito.`when`(repository.update(any(FilmItem::class.java),  FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)).then {
            isUpdateChecked = true
            Any()
        }
        Mockito.`when`(repository.delete(any(FilmItem::class.java), FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)).then {
            isDeleteChecked = true
            Any()
        }

        filmLibViewModel.insert(any(FilmItem::class.java), FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        filmLibViewModel.update(any(FilmItem::class.java), FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        filmLibViewModel.delete(any(FilmItem::class.java), FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)


        assertTrue(isInsertChecked)
        assertTrue(isUpdateChecked)
        assertTrue(isDeleteChecked)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
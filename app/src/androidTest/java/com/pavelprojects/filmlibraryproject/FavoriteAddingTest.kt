package com.pavelprojects.filmlibraryproject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pavelprojects.filmlibraryproject.ui.FilmAdapter
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteAddingTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<FilmLibraryActivity> =
        ActivityScenarioRule(FilmLibraryActivity::class.java)

    @Before
    fun start() {
    }

    @Test
    fun `Проверка_на_добавление_фильма_в_избранное`() {
        waitingForInit()
        addFilmToFavourites()
        goToFavouritesScreen()
        checkFilmDisplayedOnFavouritesScreen()
    }

    private fun waitingForInit() {
        var count = 5
        while (count>0) {
            try {
                onView(withId(R.id.recyclerView_films)).check(matches(isDisplayed()))
                count = 0
            } catch (ex: NoMatchingViewException) {
                Thread.sleep(500)
                count--
            }
        }
    }

    private fun addFilmToFavourites() {
        var count = 5
        while (count>0) {
            try {
                onView(withId(R.id.recyclerView_films))
                    .perform(RecyclerViewActions.actionOnItemAtPosition<FilmAdapter.FilmItemViewHolder>(1, ItemViewAction(R.id.materialCardView_1)))
                count = 0
            } catch (ex: PerformException) {
                Thread.sleep(500)
                count--
            }
        }
    }

    private fun goToFavouritesScreen() {
        onView(withId(R.id.menu_favorite)).perform(click())
    }

    private fun checkFilmDisplayedOnFavouritesScreen() {
        onView(withId(R.id.materialCardView_1)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView_favorite))
            .perform(RecyclerViewActions.actionOnItemAtPosition<FilmAdapter.FilmItemViewHolder>(1, ItemViewAction(R.id.materialCardView_1)))
    }

}
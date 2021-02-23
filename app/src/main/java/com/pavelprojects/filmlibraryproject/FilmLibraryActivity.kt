package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment

class FilmLibraryActivity : AppCompatActivity(), FilmListFragment.OnFilmClickListener,
    FavoriteFilmsFragment.OnFavoriteListener, FilmInfoFragment.OnInfoFragmentListener {

    companion object {
        private const val KEY_FILM_LIST = "FILM_LIST"
        private const val KEY_LIKED_FILM_LIST = "LIKED_FILM_LIST"
    }

    private var listOfFilms = arrayListOf<FilmItem>()
    private var listOfLikedFilms = arrayListOf<FilmItem>()

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }
    //private val constraintLayout by lazy { findViewById<ConstraintLayout>(R.id.constraintLayout_parent) }


    //private val fragmentContainer by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)
        initViews()
        initListeners()
        if (savedInstanceState == null)
            openFilmListFragment(listOfFilms)

    }

    private fun initViews() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.navigationView)
        bottomNavView.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.menu_home) {
                openFilmListFragment(listOfFilms)
            } else {
                openFavoriteFilmsFragment(listOfLikedFilms)
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun initListeners() {
        /*findViewById<View>(R.id.button_invite).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.message_share))
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
         */
    }

    private fun openFilmListFragment(listOfFilms: ArrayList<FilmItem>) {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                FilmListFragment.newInstance(listOfFilms),
                FilmListFragment.TAG
            )
            .commit()
    }

    private fun openFilmInfoFragment(filmItem: FilmItem) {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer, FilmInfoFragment.newInstance(filmItem),
                FilmInfoFragment.TAG
            )
            .addToBackStack(null)
            .commit()
    }

    private fun openFavoriteFilmsFragment(favoriteFilms: ArrayList<FilmItem>) {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                FavoriteFilmsFragment.newInstance(favoriteFilms),
                FavoriteFilmsFragment.TAG
            )
            .commit()
    }

    override fun onResume() {
        super.onResume()
        if (listOfFilms.isEmpty()) {
            listOfFilms.add(
                FilmItem(
                    getString(R.string.text_film_1),
                    getString(R.string.text_description_1),
                    R.drawable.joker,
                    false
                )
            )
            listOfFilms.add(
                FilmItem(
                    getString(R.string.text_film_2),
                    getString(R.string.text_description_2),
                    R.drawable.green_book,
                    false
                )
            )
            listOfFilms.add(
                FilmItem(
                    getString(R.string.text_film_3),
                    getString(R.string.text_description_3),
                    R.drawable.mulan,
                    false
                )
            )
            listOfFilms.add(
                FilmItem(
                    getString(R.string.text_film_4),
                    getString(R.string.text_description_4),
                    R.drawable.tenet,
                    false
                )
            )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        listOfFilms =
            savedInstanceState.getParcelableArrayList<FilmItem>(KEY_FILM_LIST) as ArrayList<FilmItem>
        listOfLikedFilms =
            savedInstanceState.getParcelableArrayList<FilmItem>(KEY_LIKED_FILM_LIST) as ArrayList<FilmItem>
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_FILM_LIST, listOfFilms)
        outState.putParcelableArrayList(KEY_LIKED_FILM_LIST, listOfLikedFilms)
    }

    //OnFilmCLickListener
    override fun onLikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        listOfFilms[position - 1] = filmItem
        if (!listOfLikedFilms.contains(filmItem)) {
            listOfLikedFilms.add(filmItem)
        }
        Snackbar.make(frameLayout, R.string.snackbar_like, Snackbar.LENGTH_SHORT)
            .setAction(R.string.snackbar_action) {
                listOfLikedFilms.remove(filmItem)
                listOfFilms[position - 1] = filmItem.apply { isLiked = false }
            }.show()
    }

    override fun onDislikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        listOfFilms[position - 1] = filmItem
        if (listOfLikedFilms.contains(filmItem)) {
            listOfLikedFilms.remove(filmItem)
        }
        Snackbar.make(frameLayout, R.string.snackbar_dont_like, Snackbar.LENGTH_SHORT)
            .setAction(R.string.snackbar_action) {
                listOfLikedFilms.add(filmItem)
                listOfFilms[position - 1] = filmItem.apply { isLiked = true }
            }.show()
    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        openFilmInfoFragment(filmItem)
    }

    //OnFavoriteListener
    override fun onFavoriteDeleted(item: FilmItem) {
        listOfFilms[listOfFilms.indexOf(item)].isLiked = false
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item)
    }

    //OnInfoFragmentListener
    override fun onRateButtonClicked(item: FilmItem) {
        listOfFilms[listOfFilms.indexOf(item)].isLiked = item.isLiked
        if (item.isLiked) {
            listOfLikedFilms.add(item)
        } else
            listOfLikedFilms.remove(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else
            ExitDialog.createDialog(
                supportFragmentManager,
                object : ExitDialog.OnDialogClickListener {
                    override fun onAcceptButtonCLick() {
                        finish()
                    }

                    override fun onDismissButtonClick() {

                    }
                })
    }


}
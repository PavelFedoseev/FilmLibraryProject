package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment

class FilmLibraryActivity : AppCompatActivity(), FilmListFragment.OnFilmClickListener,
    FilmInfoFragment.OnInfoFragmentListener, FavoriteFilmsFragment.OnFavoriteListener {

    val viewModel by lazy { ViewModelProvider(this).get(FilmLibraryViewModel::class.java) }

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)
        initViews()
        initListeners()
        initModel()
        if (savedInstanceState == null)
            openFilmListFragment()

    }

    private fun initViews() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.navigationView)
        bottomNavView.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.menu_home) {
                openFilmListFragment()
            } else {
                openFavoriteFilmsFragment()
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

    private fun initModel() {
        viewModel.getSnackBarString().observe(this) {
            Snackbar.make(frameLayout, it, Snackbar.LENGTH_SHORT)
                .setAction(R.string.snackbar_repeat) {
                    viewModel.initFilmDownloading()
                }.show()
        }
    }

    private fun openFilmListFragment() {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                FilmListFragment.newInstance(),
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

    private fun openFavoriteFilmsFragment() {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                FavoriteFilmsFragment.newInstance(),
                FavoriteFilmsFragment.TAG
            )
            .commit()
    }

    /*
    //OnFilmCLickListener
    override fun onLikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        //listOfFilms[position - 1] = filmItem
        if (!listOfLikedFilms.contains(filmItem)) {
            viewModel.insert(filmItem)
        }
        Snackbar.make(frameLayout, R.string.snackbar_like, Snackbar.LENGTH_SHORT)
            .setAction(R.string.snackbar_cancel) {
                viewModel.delete(filmItem)
                //listOfFilms[position - 1] = filmItem.apply { isLiked = false }
            }.show()
    }

    override fun onDislikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        //listOfFilms[position - 1] = filmItem
        if (listOfLikedFilms.contains(filmItem)) {
            viewModel.delete(filmItem, )
        }
        Snackbar.make(frameLayout, R.string.snackbar_dont_like, Snackbar.LENGTH_SHORT)
            .setAction(R.string.snackbar_cancel) {
                viewModel.insert(filmItem)
                //listOfFilms[position - 1] = filmItem.apply { isLiked = true }
            }.show()
    }

     */
    override fun onRateButtonClicked(item: FilmItem) {

    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        openFilmInfoFragment(filmItem)
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item)
    }
    /*
    //OnFavoriteListener
    override fun onFavoriteDeleted(item: FilmItem) {
        //listOfFilms[listOfFilms.indexOf(item)].isLiked = false
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item)
    }


    //OnInfoFragmentListener
    override fun onRateButtonClicked(item: FilmItem) {
        //listOfFilms[listOfFilms.indexOf(item)].isLiked = item.isLiked
        if (item.isLiked) {
            viewModel.insert(item)
        } else
            viewModel.delete(item)
    }

     */

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
package com.pavelprojects.filmlibraryproject.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.broadcast.InternetBroadcast
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import kotlinx.android.synthetic.main.activity_filmlibrary.*

class FilmLibraryActivity : AppCompatActivity(), FilmListFragment.OnFilmListFragmentAdapter,
        FilmInfoFragment.OnInfoFragmentListener, FavoriteFilmsFragment.OnFavoriteListener {

    companion object {
        const val KEY_LIST_OF_FILMS = "ListOfFilms"
    }

    val viewModel by lazy { ViewModelProvider(this).get(FilmLibraryViewModel::class.java) }

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }
    private lateinit var snackbar: Snackbar
    private lateinit var broadcast: InternetBroadcast
    private var listOfFilms = arrayListOf<FilmItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)
        initViews()
        initModel()
        if (savedInstanceState == null)
            openFilmListFragment()

    }

    private fun initViews() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.navigationView)
        bottomNavView.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.menu_home) {
                if (supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) == null)
                    openFilmListFragment()
            } else {
                if (supportFragmentManager.findFragmentByTag(FavoriteFilmsFragment.TAG) == null)
                    openFavoriteFilmsFragment()
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_LIST_OF_FILMS, listOfFilms)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        listOfFilms = savedInstanceState.getParcelableArrayList<FilmItem>(KEY_LIST_OF_FILMS)
                ?: arrayListOf()
    }

    private fun initModel() {
        viewModel.getSnackBarString().observe(this) {
            Snackbar.make(frameLayout, it, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.snackbar_repeat) {
                        viewModel.initFilmDownloading()
                    }.show()
        }
        viewModel.getSnackBarString().observe(this) {
            makeSnackBar(it, this.getString(R.string.snackbar_network_error_action))
        }


    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.pavelprojects.BroadcastReceiver").apply {
            addAction(
                    ConnectivityManager.CONNECTIVITY_ACTION)
        }
        broadcast = InternetBroadcast(
                object : InternetBroadcast.OnBroadcastReceiver {
                    override fun onOnlineStatus(isOnline: Boolean) {
                        if (isOnline) {
                            if(supportFragmentManager.fragments.size > 0)
                                (supportFragmentManager.fragments[0] as? OnLibraryActivityChild)?.onOnllineStatusChanged(isOnline)
                            dismissSnackBar()
                        } else makeSnackBar(this@FilmLibraryActivity.getString(R.string.snackbar_network_error))
                    }
                })
        registerReceiver(broadcast, filter)
    }

    private fun openFilmListFragment() {
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
        val callerFragmentTag = if (supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) != null) FilmListFragment.TAG
        else FavoriteFilmsFragment.TAG
        supportFragmentManager.popBackStack()
        supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer, FilmInfoFragment.newInstance(filmItem, callerFragmentTag),
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

    fun makeSnackBar(text: String, action: String? = null) {
        snackbar = Snackbar.make(fragmentContainer, text, Snackbar.LENGTH_INDEFINITE)
                .setAction(action) {
                    viewModel.initFilmDownloading()
                }.setAnchorView(navigationView).also { it.show() }
    }

    fun dismissSnackBar() {
        if (this::snackbar.isInitialized) {
            snackbar.dismiss()
        }
    }

    override fun onRateButtonClicked(item: FilmItem, fragmentTag: String) {
        listOfFilms.forEach {
            if (it.filmId == item.filmId)
                listOfFilms[listOfFilms.indexOf(it)] = item
        }
        viewModel.update(item, FilmLibraryViewModel.CODE_FILM_DB)
        viewModel.update(item, FilmLibraryViewModel.CODE_FAV_FILM_DB)
        if (item.isLiked) {
            viewModel.insert(item, FilmLibraryViewModel.CODE_FAV_FILM_DB)
        } else {
            viewModel.delete(item, FilmLibraryViewModel.CODE_FAV_FILM_DB)
        }
        (supportFragmentManager.findFragmentByTag(fragmentTag) as? OnLibraryActivityChild)?.onButtonRateClick(item)
    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        openFilmInfoFragment(filmItem)
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item)
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

    override fun saveListState(list: ArrayList<FilmItem>) {
        listOfFilms = list
    }

    override fun onStop() {
        unregisterReceiver(broadcast)
        super.onStop()
    }

}

interface OnLibraryActivityChild {
    fun onButtonRateClick(filmItem: FilmItem)
    fun onOnllineStatusChanged(isOnline: Boolean)
}
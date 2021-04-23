package com.pavelprojects.filmlibraryproject.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.pavelprojects.filmlibraryproject.ui.watchlater.WatchLaterFragment
import kotlinx.android.synthetic.main.activity_filmlibrary.*
import kotlinx.android.synthetic.main.fragment_film_info.*
import no.danielzeller.blurbehindlib.BlurBehindLayout

class FilmLibraryActivity : AppCompatActivity(), ActivityUpdater, FilmListFragment.OnFilmListFragmentAdapter,
    FilmInfoFragment.OnInfoFragmentListener, FavoriteFilmsFragment.OnFavoriteListener,
    WatchLaterFragment.OnWatchLaterListener {

    companion object {
        const val KEY_LIST_OF_FILMS = "ListOfFilms"
    }

    val viewModel by lazy { ViewModelProvider(this).get(FilmLibraryViewModel::class.java) }

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }
    private lateinit var snackbar: Snackbar
    private lateinit var broadcast: InternetBroadcast
    private val imageButtonWL: View by lazy { findViewById(R.id.imageButton_wl) }
    private val blurAppBar: BlurBehindLayout by lazy {findViewById(R.id.topBarBlurLayout)}
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
        imageButtonWL.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag(WatchLaterFragment.TAG) == null)
                openWatchLatterFragment()
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
                ConnectivityManager.CONNECTIVITY_ACTION
            )
        }
        broadcast = InternetBroadcast(
            object : InternetBroadcast.OnBroadcastReceiver {
                override fun onOnlineStatus(isOnline: Boolean) {
                    if (isOnline) {
                        if (supportFragmentManager.fragments.size > 0)
                            (supportFragmentManager.fragments[0] as? LibraryActivityChild)?.onOnllineStatusChanged(
                                isOnline
                            )
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

    private fun openWatchLatterFragment() {
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer, WatchLaterFragment.newInstance(),
                WatchLaterFragment.TAG
            )
            .addToBackStack(WatchLaterFragment.TAG)
            .commit()
    }

    private fun openFilmInfoFragment(filmItem: FilmItem) {
        val callerFragmentTag = when {
            supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) != null -> FilmListFragment.TAG
            supportFragmentManager.findFragmentByTag(FavoriteFilmsFragment.TAG) != null -> FavoriteFilmsFragment.TAG
            else -> WatchLaterFragment.TAG
        }
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer, FilmInfoFragment.newInstance(filmItem, callerFragmentTag),
                FilmInfoFragment.TAG
            )
            .addToBackStack(FilmInfoFragment.TAG)
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
        viewModel.update(item, FilmLibraryViewModel.CODE_FILM_TABLE)
        viewModel.update(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        if (item.isLiked || item.isWatchLater) {
            viewModel.insert(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        } else {
            viewModel.delete(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        }
        (supportFragmentManager.findFragmentByTag(fragmentTag) as? LibraryActivityChild)?.onButtonRateClick(
            item
        )
    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        openFilmInfoFragment(filmItem)
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item)
    }

    override fun onWatchLaterDetail(item: FilmItem) {
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

    override fun setupBlur(view: View) {
        blurAppBar.viewBehind = view
        if(blurAppBar.visibility == View.GONE){
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_show_bar).apply {
                setAnimationListener(object: Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        blurAppBar.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(p0: Animation?) {

                    }
                })
            }
            blurAppBar.startAnimation(animation)
        }
    }

    override fun hideAppBar() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_hide_bar).apply {
            setAnimationListener(object: Animation.AnimationListener{
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    blurAppBar.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }
            })
        }
        blurAppBar.startAnimation(animation)
    }
}

interface LibraryActivityChild {
    fun onButtonRateClick(filmItem: FilmItem)
    fun onOnllineStatusChanged(isOnline: Boolean)
}
interface ActivityUpdater{
    fun setupBlur(view: View)
    fun hideAppBar()
}
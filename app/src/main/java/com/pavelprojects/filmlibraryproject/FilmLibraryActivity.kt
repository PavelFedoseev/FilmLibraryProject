package com.pavelprojects.filmlibraryproject

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.broadcast.InternetBroadcast
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import kotlinx.android.synthetic.main.activity_filmlibrary.*

class FilmLibraryActivity : AppCompatActivity(), FilmListFragment.OnFilmClickListener,
    FilmInfoFragment.OnInfoFragmentListener, FavoriteFilmsFragment.OnFavoriteListener {

    val viewModel by lazy { ViewModelProvider(this).get(FilmLibraryViewModel::class.java) }

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }
    private lateinit var snackbar: Snackbar
    private lateinit var broadcast: InternetBroadcast

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
        viewModel.getSnackBarString().observe(this) {
            makeSnackBar(it, this.getString(R.string.snackbar_network_error_action))
        }
        val filter = IntentFilter("com.pavelprojects.BroadcastReceiver").apply { addAction(
            ConnectivityManager.CONNECTIVITY_ACTION)}
        broadcast = InternetBroadcast(
            object : InternetBroadcast.OnBroadcastReceiver {
                override fun onOnlineStatus(isOnline: Boolean) {
                    if(isOnline) {
                        viewModel.downloadPopularMovies()
                        dismissSnackBar()
                    }
                    else makeSnackBar(this@FilmLibraryActivity.getString(R.string.snackbar_network_error))
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

    fun makeSnackBar(text: String, action: String? = null) {
        snackbar = Snackbar.make(constraintLayoutParent, text, Snackbar.LENGTH_INDEFINITE)
                .setAction(action) {
                    viewModel.initFilmDownloading()
                }.also { it.show() }
    }

    fun dismissSnackBar() {
        if(this::snackbar.isInitialized){
            snackbar.dismiss()
        }
    }

    override fun onRateButtonClicked(item: FilmItem) {

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

    override fun onDestroy() {
        super.onDestroy()
    }
}
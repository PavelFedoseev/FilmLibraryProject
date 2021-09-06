package com.pavelprojects.filmlibraryproject.ui

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.broadcast.InternetBroadcast
import com.pavelprojects.filmlibraryproject.broadcast.ReminderBroadcast
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toFilmItem
import com.pavelprojects.filmlibraryproject.di.ViewModelFactory
import com.pavelprojects.filmlibraryproject.firebase.NotificationFirebaseService
import com.pavelprojects.filmlibraryproject.ui.about.AboutDialogFragment
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FilmLibraryViewModel
import com.pavelprojects.filmlibraryproject.ui.watchlater.WatchLaterFragment
import kotlinx.android.synthetic.main.activity_filmlibrary.*
import kotlinx.android.synthetic.main.fragment_film_info.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.danielzeller.blurbehindlib.BlurBehindLayout
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FilmLibraryActivity : AppCompatActivity(), ActivityUpdater,
    FilmListFragment.OnFilmListFragmentAdapter,
    FilmInfoFragment.OnInfoFragmentListener, FavoriteFilmsFragment.OnFavoriteListener,
    WatchLaterFragment.OnWatchLaterListener {

    companion object {
        const val KEY_LIST_OF_FILMS = "ListOfFilms"
        const val TAG = "FilmLibraryActivity"
        const val NOTIF_REQUEST_PERM = 1111
    }

    @Inject
    lateinit var application: App

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(
            FilmLibraryViewModel::class.java
        )
    }

    private lateinit var snackbar: Snackbar
    private lateinit var broadcast: InternetBroadcast
    private val textViewAppName: TextView by lazy { findViewById(R.id.text_top_bar_name)}
    private val blurAppBar: BlurBehindLayout by lazy { findViewById(R.id.topBarBlurLayout) }
    private val blurNavigationView: BlurBehindLayout by lazy { findViewById(R.id.navigationBarBlurLayout) }
    private val editTextSearch: EditText by lazy {
        findViewById(R.id.appBarSearch)
    }

    private val buttonSearch: ImageButton by lazy {
        findViewById(R.id.appBarSearchButton)
    }
    private val searchCard: MaterialCardView by lazy {
        findViewById(R.id.appBarCardSearch)
    }
    private var listOfFilms = arrayListOf<FilmItem>()
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_filmlibrary)
        App.appComponent.inject(this)
        checkAndRequestPermissions()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        initViews()
        initModel()

        if (supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) == null){
            searchCard.visibility = View.GONE
        }
        if (savedInstanceState == null) {
            openFilmListFragment()
            processIntent(intent)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(TAG).w(task.exception, "Fetching FCM registration token failed")
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            val msg = token.toString()
            Timber.tag(TAG).d(msg)
        })

        if (editTextSearch.text.isNotEmpty()) {
            buttonSearch.visibility = View.VISIBLE
        } else {
            buttonSearch.visibility = View.GONE
        }
    }

    private fun processIntent(intent: Intent) {
        val bundle = intent.getBundleExtra(ReminderBroadcast.BUNDLE_OUT)
        if (bundle != null) {
            val item = bundle.getParcelable<ChangedFilmItem>(ReminderBroadcast.BUNDLE_FILMITEM)
            item?.let { openFilmInfoFragment(item.id) }
        } else {
            val filmId = intent.getStringExtra(NotificationFirebaseService.INTENT_FILM_CODE)
            if (filmId != null) {
                openFilmInfoFragment(Integer.parseInt(filmId))
            }
        }
    }

    private fun initModel() {
        viewModel.isSearchMode.observe(this) { status ->
            isSearchMode = status
            if (status) {
                buttonSearch.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_cancel,
                        null
                    )
                )
            } else {
                buttonSearch.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_search,
                        null
                    )
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun initViews() {
        appBarDimmer.layoutParams = getStatusBarHeightParams()
        val bottomNavView = findViewById<BottomNavigationView>(R.id.navigationView)
        bottomNavView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_home -> if (supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) == null)
                    openFilmListFragment()
                R.id.menu_favorite -> if (supportFragmentManager.findFragmentByTag(
                        FavoriteFilmsFragment.TAG
                    ) == null
                )
                    openFavoriteFilmsFragment()
                R.id.menu_watch_later -> if (supportFragmentManager.findFragmentByTag(
                        WatchLaterFragment.TAG
                    ) == null
                )
                    openWatchLatterFragment()
            }

            return@setOnNavigationItemSelectedListener true
        }

        textViewAppName.setOnClickListener {
            openAboutFragment()
        }

        editTextSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.onEditTextSearchChanged()
            if (text != null && text.isNotEmpty()) {
                buttonSearch.visibility = View.VISIBLE
            } else {
                buttonSearch.visibility = View.GONE
            }
        }
        editTextSearch.setOnEditorActionListener { _, i, _ ->
            var handled = false
            if (i == EditorInfo.IME_ACTION_DONE && editTextSearch.text.isNotEmpty()) {
                viewModel.onSearchBarButtonClicked(editTextSearch.text.toString())
                editTextSearch.clearFocus()
                handled = true
            }
            handled
        }
        buttonSearch.setOnClickListener {
            if(isSearchMode && editTextSearch.text.isNotEmpty()){
                editTextSearch.text.clear()
            }
            editTextSearch.clearFocus()
            viewModel.onSearchBarButtonClicked(editTextSearch.text.toString())
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_LIST_OF_FILMS, listOfFilms)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        listOfFilms = savedInstanceState.getParcelableArrayList(KEY_LIST_OF_FILMS)
            ?: arrayListOf()
    }

    private fun checkAndRequestPermissions() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Timber.tag(TAG).d("Notification permission sending request...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                NOTIF_REQUEST_PERM
            )
        } else {
            Timber.tag(TAG).d("Notification permission granted")
            this.application.createNotificationChannel()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIF_REQUEST_PERM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.tag(TAG).d("Notification permission granted")
                application.createNotificationChannel()
            } else {
                Timber.tag(TAG).d("Notification permission denied")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initInternetBroadcast()
    }

    private fun initInternetBroadcast() {
        val filter = IntentFilter("com.pavelprojects.BroadcastReceiver").apply {
            addAction(
                ConnectivityManager.CONNECTIVITY_ACTION
            )
        }
        broadcast = InternetBroadcast(
            object : InternetBroadcast.OnBroadcastReceiver {
                override fun onOnlineStatus(isOnline: Boolean) {
                    if (supportFragmentManager.fragments.size > 0)
                        (supportFragmentManager.fragments[0] as? OnlineStatusUpdater)?.onOnlineStatusChanged(
                            isOnline
                        )
                    viewModel.onOnlineStatusChanged(isOnline)
                    if (isOnline) dismissSnackBar() else makeSnackBar(
                        this@FilmLibraryActivity.getString(
                            R.string.snackbar_network_error
                        )
                    )
                }
            })
        registerReceiver(broadcast, filter)
    }

    private fun openFilmListFragment() {
        searchCard.visibility = View.VISIBLE
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
        searchCard.visibility = View.GONE
        supportFragmentManager.popBackStack()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer, WatchLaterFragment.newInstance(),
                WatchLaterFragment.TAG
            )
            .commit()
    }

    private fun openAboutFragment(){
        val dialog = AboutDialogFragment.newInstance()
        dialog.show(supportFragmentManager, AboutDialogFragment.TAG)
    }

    private fun openFilmInfoFragment(filmId: Int) {
        val callerFragmentTag = when {
            supportFragmentManager.findFragmentByTag(FilmListFragment.TAG) != null -> FilmListFragment.TAG
            supportFragmentManager.findFragmentByTag(FavoriteFilmsFragment.TAG) != null -> FavoriteFilmsFragment.TAG
            else -> WatchLaterFragment.TAG
        }
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer, FilmInfoFragment.newInstance(filmId, callerFragmentTag),
                FilmInfoFragment.TAG
            )
            .addToBackStack(FilmInfoFragment.TAG)
            .commit()
    }

    private fun openFavoriteFilmsFragment() {
        searchCard.visibility = View.GONE
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

    override fun makeSnackBar(text: String, length: Int, action: String?) {
        snackbar = Snackbar.make(fragmentContainer, text, length)
            .setAction(action) {

            }.setAnchorView(navigationView).also { it.show() }
    }

    fun dismissSnackBar() {
        if (this::snackbar.isInitialized) {
            snackbar.dismiss()
        }
    }

    override fun onRateButtonClicked(changedFilmItem: ChangedFilmItem, fragmentTag: String) {
        val item = changedFilmItem.toFilmItem()
        listOfFilms.forEach {
            if (it.id == item.id)
                listOfFilms[listOfFilms.indexOf(it)] = item
        }
        viewModel.onRateButtonClicked(item, changedFilmItem)
    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int) {
        openFilmInfoFragment(filmItem.id)
    }

    override fun onFavoriteDetail(item: FilmItem) {
        openFilmInfoFragment(item.id)
    }

    override fun onWatchLaterDetail(item: FilmItem) {
        openFilmInfoFragment(item.id)
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

    override fun setupBlur(view: View, setupBar: Boolean) {
        try {
            if (setupBar) {
                blurAppBar.viewBehind = view
                if (blurAppBar.visibility == View.GONE) {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.anim_show_bar).apply {
                        setAnimationListener(object : Animation.AnimationListener {
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
            blurNavigationView.viewBehind = view
        } catch (e: Exception) {
            Timber.tag(TAG).e(e.toString())
        }
    }

    override fun hideAppBar() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_hide_bar).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    blurAppBar.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }
            })
        }
        blurAppBar.viewBehind = null
        blurAppBar.startAnimation(animation)
    }

    private fun getStatusBarHeightParams(): FrameLayout.LayoutParams {

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val margin = resources.getDimensionPixelSize(resourceId)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParams.topMargin = margin
        return layoutParams
    }
}

interface OnlineStatusUpdater {
    fun onOnlineStatusChanged(isOnline: Boolean)
}

interface ActivityUpdater {
    fun setupBlur(view: View, setupBar: Boolean = true)
    fun hideAppBar()
    fun makeSnackBar(text: String, length: Int = Snackbar.LENGTH_INDEFINITE, action: String? = null)
}


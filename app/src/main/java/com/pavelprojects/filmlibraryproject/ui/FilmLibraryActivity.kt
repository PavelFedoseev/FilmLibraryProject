package com.pavelprojects.filmlibraryproject.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import com.pavelprojects.filmlibraryproject.ui.favorites.FavoriteFilmsFragment
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
import com.pavelprojects.filmlibraryproject.ui.watchlater.WatchLaterFragment
import kotlinx.android.synthetic.main.activity_filmlibrary.*
import kotlinx.android.synthetic.main.fragment_film_info.*
import no.danielzeller.blurbehindlib.BlurBehindLayout
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

    private val viewModel by lazy { ViewModelProvider(this, viewModelFactory).get(FilmLibraryViewModel::class.java) }

    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }

    private lateinit var snackbar: Snackbar
    private lateinit var broadcast: InternetBroadcast
    private val blurAppBar: BlurBehindLayout by lazy { findViewById(R.id.topBarBlurLayout) }
    private val blurNavigationView: BlurBehindLayout by lazy { findViewById(R.id.navigationBarBlurLayout) }
    private var listOfFilms = arrayListOf<FilmItem>()

    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_filmlibrary)
        App.appComponent.inject(this)
        checkAndRequestPermissions()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        initViews()
        initModel()

        if (savedInstanceState == null) {
            application.loadedPage = 1
            openFilmListFragment()
            bundle = intent.getBundleExtra(ReminderBroadcast.BUNDLE_OUT)
            if (bundle != null) {
                val item = bundle?.getParcelable<ChangedFilmItem>(ReminderBroadcast.BUNDLE_FILMITEM)
                item?.let { openFilmInfoFragment(item.toFilmItem()) }
            } else {
                val filmId = intent.getStringExtra(NotificationFirebaseService.INTENT_FILM_CODE)
                if (filmId != null) {
                    viewModel.getFilmById(filmId.toInt()).observe(this) {
                        if (it != null) {
                            openFilmInfoFragment(it)
                        }
                    }
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            val msg = token.toString()
            Log.d(TAG, msg)
        })


    }

    private fun initViews() {
        appBarDimmer.layoutParams = getStatusBarHeightParams()
//        navigationBarBlurLayout.layoutParams = getNavigationBarHeightParams(this, resources.configuration.orientation)
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

        viewModel.getWatchLatter().observe(this) {
            updateNotificationChannel(this, it)
        }


    }

    private fun checkAndRequestPermissions() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.d(TAG, "Notification permission sending request...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                NOTIF_REQUEST_PERM
            )
        } else {
            Log.d(TAG, "Notification permission granted")
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
                Log.d(TAG, "Notification permission granted")
                application.createNotificationChannel()
            } else {
                Log.d(TAG, "Notification permission denied")
            }
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
                            (supportFragmentManager.fragments[0] as? LibraryActivityChild)?.onOnlineStatusChanged(
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

    override fun makeSnackBar(text: String, length: Int, action: String?) {
        snackbar = Snackbar.make(fragmentContainer, text, length)
            .setAction(action) {
                viewModel.initFilmDownloading()
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
        viewModel.update(item, FilmLibraryViewModel.CODE_FILM_TABLE)
        viewModel.update(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        if (item.isLiked || item.isWatchLater) {
            viewModel.insert(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        } else {
            viewModel.delete(item, FilmLibraryViewModel.CODE_CHANGED_FILM_TABLE)
        }
        if (changedFilmItem.watchLaterDate != -1L) {
            updateNotificationChannel(this, listOf(changedFilmItem))
            viewModel.updateChanged(changedFilmItem)
        }
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
        viewModel.deleteAllIncorrect()
        super.onStop()
    }

    override fun setupBlur(view: View) {
        blurAppBar.disable()
        blurNavigationView.disable()
        blurAppBar.viewBehind = view
        blurNavigationView.viewBehind = view
        if (blurAppBar.visibility == View.GONE) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_show_bar).apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        blurAppBar.visibility = View.VISIBLE
                        blurAppBar.enable()
                        blurNavigationView.enable()
                    }

                    override fun onAnimationRepeat(p0: Animation?) {

                    }
                })
            }
            blurAppBar.startAnimation(animation)
        }
        else {
            blurAppBar.enable()
            blurNavigationView.enable()
        }
    }

    override fun disableBlur() {
        blurAppBar.disable()
        blurNavigationView.disable()
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

//    private fun getNavigationBarHeightParams(context: Context, orientation: Int): ConstraintLayout.LayoutParams {
//        val resources = context.resources
//        var id: Int = resources.getIdentifier(
//            if (orientation == Configuration.ORIENTATION_PORTRAIT) "navigation_bar_height" else "navigation_bar_height_landscape",
//            "dimen",
//            "android"
//        )
//        id = if (id > 0) {
//            resources.getDimensionPixelSize(id)
//        } else 0
//        return ConstraintLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            resources.getDimensionPixelSize(R.dimen.nav_bar_size)
//        ).apply { topMargin = id
//        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID}
//    }


    override fun updateNotificationChannel(context: Context, list: List<ChangedFilmItem>) {

        val alarmManager = getSystemService(ALARM_SERVICE) as? AlarmManager
        for (item in list) {
            if (item.watchLaterDate != -1L && item.watchLaterDate >= Calendar.getInstance().timeInMillis) {
                val intent = Intent(context, ReminderBroadcast::class.java)
                val bundle =
                    Bundle().apply { putParcelable(ReminderBroadcast.BUNDLE_FILMITEM, item) }
                intent.putExtra(ReminderBroadcast.INTENT_FILMITEM_BUNDLE, bundle)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    item.id ?: 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager?.set(AlarmManager.RTC_WAKEUP, item.watchLaterDate, pendingIntent)
            }
        }
    }
}

interface LibraryActivityChild {
    fun onOnlineStatusChanged(isOnline: Boolean)
}

interface ActivityUpdater {
    fun setupBlur(view: View)
    fun disableBlur()
    fun hideAppBar()
    fun updateNotificationChannel(context: Context, list: List<ChangedFilmItem>)
    fun makeSnackBar(text: String, length: Int = Snackbar.LENGTH_INDEFINITE, action: String? = null)
}
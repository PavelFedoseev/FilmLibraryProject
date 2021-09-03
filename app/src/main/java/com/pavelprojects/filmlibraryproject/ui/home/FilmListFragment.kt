package com.pavelprojects.filmlibraryproject.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.di.ViewModelFactory
import com.pavelprojects.filmlibraryproject.domain.extentions.compare
import com.pavelprojects.filmlibraryproject.ui.*
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class FilmListFragment : Fragment(), OnlineStatusUpdater {
    companion object {
        const val TAG = "FilmListFragment"
        const val KEY_LIST = "FilmList"
        fun newInstance(list: ArrayList<FilmItem> = arrayListOf()): FilmListFragment {
            Log.d(TAG, "newInstance")
            val bundle = Bundle().apply {
                putParcelableArrayList(KEY_LIST, list)
            }
            return FilmListFragment().apply { arguments = bundle }
        }
    }

    @Inject
    lateinit var application: App

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val mDisposable = CompositeDisposable()

    private lateinit var adapter: FilmPagingAdapter
    private var isConnected = false

    private var curSource = FilmSource.REMOTE

    private val viewModel: FilmLibraryViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(FilmLibraryViewModel::class.java)
    }

    var listOfFilms: ArrayList<FilmItem> = arrayListOf()
    var orientation = 0
    private var position = 0

    lateinit var layoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = FilmPagingAdapter(
            requireContext().getString(R.string.label_library),
            listener = object : FilmPagingAdapter.FilmClickListener() {
                override fun onDetailClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {
                    (activity as? OnFilmListFragmentAdapter)?.onDetailClicked(
                        filmItem,
                        position,
                        adapterPosition
                    )
                }

                override fun onDoubleClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {
                    filmItem.isLiked = !filmItem.isLiked
                    (activity as? FilmInfoFragment.OnInfoFragmentListener)?.onRateButtonClicked(
                        filmItem.toChangedFilmItem(),
                        TAG
                    )
                }
            })
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_filmlist, container, false)
        position = viewModel.getRecyclerSavedPos()
        orientation = resources.configuration.orientation
        listOfFilms = arguments?.getParcelableArrayList(KEY_LIST) ?: arrayListOf()
        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            GridLayoutManager(requireContext(), 2)
        else
            GridLayoutManager(requireContext(), 4)

        initRecycler(view, position)
        initModel()
        (activity as? ActivityUpdater)?.setupBlur(view)
        return view
    }


    @ExperimentalCoroutinesApi
    private fun initModel() {
        Log.d(TAG, "initModel")
        var position: Int

        viewModel.subscribeToDatabase().observe(this.viewLifecycleOwner) {
            if (it != null) {
                position = listOfFilms.size
                listOfFilms.addAll(it.compare(listOfFilms))
                if(viewModel.getLoadedPage() == 1 && !isConnected)
                    initLocalSource()
            }
        }
        viewModel.isConnectionStatus.observe(this.viewLifecycleOwner){ isConnected ->
            this.isConnected = isConnected
            if(isConnected){
                initRemoteSource()
            }
            else {
                viewModel.getCachedFilmList()
            }
        }

        viewModel.observeAllChanged().observe(this.viewLifecycleOwner) {
            listOfFilms.iterator().forEach { item ->
                it.iterator().forEach { item1 ->
                    if (item1.id == item.id) {
                        item.isLiked = item1.isLiked
                        item.isWatchLater = item1.isWatchLater
                    }
                }
            }
        }

        viewModel.observeSnackBarString().observe(this.viewLifecycleOwner) {

        }
        viewModel.observeNetworkLoadingStatus().observe(this.viewLifecycleOwner){ isLoading ->
            if(!isLoading){
                (activity as? FilmLibraryActivity)?.makeSnackBar(
                    resources.getString(R.string.snackbar_download_error),
                    action = resources.getString(R.string.snackbar_repeat)
                )
            }
            else (activity as? FilmLibraryActivity)?.dismissSnackBar()
        }
        viewModel.getPopularFilms().observe(this.viewLifecycleOwner){ flowable ->
            if(flowable!= null)
            mDisposable.add(flowable.subscribe {
                adapter.submitData(lifecycle, it)
            })
        }
        viewModel.onObserversInitialized()
    }


    private fun initRecycler(view: View, position: Int = 0) {
        Log.d(TAG, "initRecycler")
        recyclerView = view.findViewById(R.id.recyclerView_films)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    FilmPagingAdapter.VIEW_TYPE_HEADER -> {
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) 2
                        else 4
                    }
                    FilmPagingAdapter.VIEW_TYPE_FILM -> 1
                    else -> 1
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.adapter = adapter.withLoadStateFooter(LoadingGridStateAdapter())
        adapter.addLoadStateListener {
                loadState ->
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let {
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.snackbar_network_error)
                    .setMessage(it.error.localizedMessage)
                    .setNegativeButton(R.string.snackbar_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.snackbar_repeat) { _, _ ->
                        adapter.retry()
                    }
                    .show()
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.onRecyclerScrolled(layoutManager.findLastVisibleItemPosition())
            }
        })
        //recyclerView.itemAnimator = FilmItemAnimator(requireContext())
    }

    @ExperimentalCoroutinesApi
    private fun initRemoteSource(){
        viewModel.onInitRemoteSource(curSource == FilmSource.LOCAL)
        curSource = FilmSource.REMOTE
    }
    private fun initLocalSource(){
        adapter.submitData(lifecycle, PagingData.from(listOfFilms))
        curSource = FilmSource.LOCAL
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        if (savedInstanceState != null) {
            listOfFilms = savedInstanceState.getParcelableArrayList(KEY_LIST) ?: arrayListOf()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        outState.putParcelableArrayList(KEY_LIST, listOfFilms)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        (activity as? OnFilmListFragmentAdapter)?.saveListState(listOfFilms)
        super.onDestroy()
    }

    private fun countRecyclerPos(){
        val pastVisibleItem = layoutManager.findFirstVisibleItemPosition()
        viewModel.onRecyclerScrolled(pastVisibleItem)
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            (activity as? ActivityUpdater)?.setupBlur(requireView())
        }
    }

    override fun onDestroyView() {
        countRecyclerPos()
        mDisposable.dispose()
        super.onDestroyView()
    }

    override fun onOnlineStatusChanged(isOnline: Boolean) {
        viewModel.onOnlineStatusChanged(isOnline)
        adapter.retry()
    }

    interface OnFilmListFragmentAdapter {
        fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun saveListState(list: ArrayList<FilmItem>)
    }

}

enum class FilmSource{
    REMOTE,
    LOCAL
}
package com.pavelprojects.filmlibraryproject.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.di.ViewModelFactory
import com.pavelprojects.filmlibraryproject.domain.extentions.compare
import com.pavelprojects.filmlibraryproject.ui.*
import com.pavelprojects.filmlibraryproject.ui.FilmItemAnimator.Companion.TAG_LIKE_ANIM
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FilmLibraryViewModel
import com.pavelprojects.filmlibraryproject.ui.viewmodel.FilmSource
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class FilmListFragment : Fragment(), OnlineStatusUpdater {
    companion object {
        const val TAG = "FilmListFragment"
        const val KEY_LIST = "FilmList"
        fun newInstance(list: ArrayList<FilmItem> = arrayListOf()): FilmListFragment {
            Timber.tag(TAG).d("newInstance")
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
    private var isConnected = true

    private var curSource = FilmSource.REMOTE

    private var alertDialog: AlertDialog? = null

    private val viewModel: FilmLibraryViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(FilmLibraryViewModel::class.java)
    }

    var listOfFilms: ArrayList<FilmItem> = arrayListOf()
    var orientation = 0
    private var position = 0

    lateinit var layoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout

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
                    adapter.notifyItemChanged(position, TAG_LIKE_ANIM)
                }
            })
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag(TAG).d("onCreateView")
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
        Timber.tag(TAG).d("initModel")
        var position: Int

        viewModel.subscribeToDatabase().observe(this.viewLifecycleOwner) {
            if (it != null) {
                position = listOfFilms.size
                listOfFilms.addAll(it.compare(listOfFilms))
                if (viewModel.getInitState() && !isConnected)
                    adapter.submitData(lifecycle, PagingData.from(listOfFilms))
            }
        }
        viewModel.isConnectionStatus.observe(this.viewLifecycleOwner) { isConnected ->
            this.isConnected = isConnected
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
        viewModel.getFilmSourceFlowable().observe(this.viewLifecycleOwner) { flowable ->
            if (flowable != null)
                mDisposable.add(flowable.subscribe {
                    adapter.submitData(lifecycle, it)
                })
            swipeLayout.isRefreshing = false
        }
        viewModel.onFragmentCreated()
    }


    private fun initRecycler(view: View, position: Int = 0) {
        Timber.tag(TAG).d("initRecycler")
        recyclerView = view.findViewById(R.id.recyclerView_films)
        swipeLayout = view.findViewById(R.id.layout_swipe_refresh)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    FilmPagingAdapter.VIEW_TYPE_HEADER -> {
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) 2
                        else 4
                    }
                    FilmPagingAdapter.VIEW_TYPE_FOOTER -> {
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
        adapter.addLoadStateListener { loadState ->
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            if (errorState == null) {
                swipeLayout.isRefreshing = false
            }
            errorState?.let {
                alertDialog?.dismiss()
                alertDialog =
                    AlertDialog.Builder(view.context).setTitle(R.string.snackbar_network_error)
                        .setMessage(it.error.localizedMessage)
                        .setNegativeButton(R.string.snackbar_cancel) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(R.string.snackbar_repeat) { _, _ ->
                            adapter.retry()
                        }
                        .create()
                alertDialog?.show()
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.onRecyclerScrolled(layoutManager.findLastVisibleItemPosition())
            }
        })
        swipeLayout.setProgressViewOffset(true, 0, resources.getDimensionPixelOffset(R.dimen.appbar_refresh_offset))
        swipeLayout.setOnRefreshListener {
            viewModel.onRefreshOccurred()
            if (!isConnected) {
                swipeLayout.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.snackbar_network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(TAG).d("onActivityCreated")
        if (savedInstanceState != null) {
            listOfFilms = savedInstanceState.getParcelableArrayList(KEY_LIST) ?: arrayListOf()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.tag(TAG).d("onSaveInstanceState")
        outState.putParcelableArrayList(KEY_LIST, listOfFilms)
    }

    override fun onDestroy() {
        Timber.tag(TAG).d("onDestroy")
        (activity as? OnFilmListFragmentAdapter)?.saveListState(listOfFilms)
        super.onDestroy()
    }

    private fun countRecyclerPos() {
        val pastVisibleItem = layoutManager.findFirstVisibleItemPosition()
        viewModel.onRecyclerScrolled(pastVisibleItem)
    }

    override fun onResume() {
        super.onResume()
        recyclerView.itemAnimator = FilmItemAnimator(requireContext())
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
        isConnected = isOnline
        adapter.retry()
    }

    interface OnFilmListFragmentAdapter {
        fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun saveListState(list: ArrayList<FilmItem>)
    }

}
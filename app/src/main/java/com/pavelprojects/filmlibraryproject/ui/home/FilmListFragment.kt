package com.pavelprojects.filmlibraryproject.ui.home

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.*
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.di.ViewModelFactory
import com.pavelprojects.filmlibraryproject.ui.*
import com.pavelprojects.filmlibraryproject.ui.FilmItemAnimator.Companion.TAG_LIKE_ANIM
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.vm.FilmLibraryViewModel
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


    private fun initModel() {
        Log.d(TAG, "initModel")
        var position: Int

        viewModel.subscribeToDatabase().observe(this.viewLifecycleOwner) {
            if (it != null && viewModel.getLoadedPage() == 1) {
                position = listOfFilms.size
                if (!listOfFilms.containsAll(it)) {
                    listOfFilms.addAll(it)
                    recyclerView.adapter?.notifyItemRangeInserted(
                        position,
                        listOfFilms.size
                    ) // size + 1 Footer
                }
            }
        }
        viewModel.subscribeToDownloads().observe(this.viewLifecycleOwner) {
            if (it != null) {
                if (viewModel.getLoadedPage() == 2) {
                    listOfFilms.clear()
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                position = listOfFilms.size
                if (!listOfFilms.containsAll(it)) {
                    listOfFilms.addAll(it)
                    recyclerView.adapter?.notifyItemRangeInserted(
                        position + 2,
                        listOfFilms.size
                    ) // size + 1 Footer
                }
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
            recyclerView.adapter?.notifyDataSetChanged()
        }
        viewModel.initModelDownloads()
        viewModel.observeSnackBarString().observe(this.viewLifecycleOwner) {
            (activity as? FilmLibraryActivity)?.makeSnackBar(it, action = resources.getString(R.string.snackbar_repeat))
        }

    }


    private fun initRecycler(view: View, position: Int = 0) {
        Log.d(TAG, "initRecycler")
        recyclerView = view.findViewById(R.id.recyclerView_films)
        val adapter = FilmAdapter(
            listOfFilms,
            requireContext().getString(R.string.label_library),
            viewModel,
            listener = object : FilmAdapter.FilmClickListener() {
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
                    listOfFilms[position - 1] = filmItem
                    recyclerView.adapter?.notifyItemChanged(position, TAG_LIKE_ANIM)
                }
            })
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    FilmAdapter.VIEW_TYPE_HEADER -> {
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) 2
                        else 4
                    }
                    FilmAdapter.VIEW_TYPE_FILM -> 1
                    else -> 2
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = FilmItemAnimator(requireContext())

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val pastVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val viewCount = adapter.itemCount - 2 // - 2 Header + Footer
                this@FilmListFragment.position = pastVisibleItem
                viewModel.onRecyclerScrolled(pastVisibleItem, visibleItemCount, viewCount)
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        if (position > 0 && listOfFilms.size > position)
            recyclerView.scrollToPosition(position)
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

    override fun onResume() {
        super.onResume()
        view?.let {
            (activity as? ActivityUpdater)?.setupBlur(requireView())
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    override fun onOnlineStatusChanged() {
            viewModel.onOnlineStatusChanged()
    }

    interface OnFilmListFragmentAdapter {
        fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun saveListState(list: ArrayList<FilmItem>)
    }

}
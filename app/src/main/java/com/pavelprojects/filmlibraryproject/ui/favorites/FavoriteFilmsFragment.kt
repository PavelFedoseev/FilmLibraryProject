package com.pavelprojects.filmlibraryproject.ui.favorites

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toChangedFilmItem
import com.pavelprojects.filmlibraryproject.di.ViewModelFactory
import com.pavelprojects.filmlibraryproject.ui.ActivityUpdater
import com.pavelprojects.filmlibraryproject.ui.FilmAdapter
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import com.pavelprojects.filmlibraryproject.ui.vm.ChangedViewModel
import com.pavelprojects.filmlibraryproject.ui.vm.NetworkLoadChecker
import javax.inject.Inject


class FavoriteFilmsFragment : Fragment() {

    companion object {
        const val TAG = "Favorite Fragment"
        fun newInstance() = FavoriteFilmsFragment()
    }

    @Inject
    lateinit var application: App

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: ChangedViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ChangedViewModel::class.java)
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var listOfFavorite: ArrayList<FilmItem>

    private var position: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_films, container, false)
        listOfFavorite = arrayListOf()
        recyclerView = view.findViewById(R.id.recyclerView_favorite)
        position = viewModel.getRecyclerSavedPos()
        initModel()
        initRecycler(position)
        (activity as? ActivityUpdater)?.setupBlur(view)
        return view
    }

    private fun initModel() {
        viewModel.observeFavFilms().observe(this.viewLifecycleOwner) {
            listOfFavorite.clear()
            listOfFavorite.addAll(it)
            recyclerView.adapter?.notifyDataSetChanged()
            if (listOfFavorite.isEmpty()) {
                recyclerView.background = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.background_recycler_favorite,
                    null
                )
            } else recyclerView.background = null
        }
    }


    private fun initRecycler(position: Int = 0) {
        val orientation = resources.configuration.orientation
        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            GridLayoutManager(requireContext(), 2)
        else
            GridLayoutManager(requireContext(), 4)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = FilmAdapter(
            listOfFavorite,
            requireContext().getString(R.string.label_favorite),
            viewModel as NetworkLoadChecker,
            false,
            object : FilmAdapter.FilmClickListener() {

                override fun onDetailClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {
                    //val extras = FragmentNavigatorExtras(view to "imageview_film_info")
                    (activity as? OnFavoriteListener)?.onFavoriteDetail(filmItem)
                }

                override fun onDoubleClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {
                    listOfFavorite.remove(filmItem)
                    filmItem.isLiked = false
                    (activity as? FilmInfoFragment.OnInfoFragmentListener)?.onRateButtonClicked(
                        filmItem.toChangedFilmItem(),
                        TAG
                    )
                    if (listOfFavorite.isEmpty()) {
                        recyclerView.background = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.background_recycler_favorite,
                            null
                        )
                    } else recyclerView.background = null
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.message_favorite_delete),
                        Toast.LENGTH_SHORT
                    ).show()
                    recyclerView.adapter?.notifyItemRemoved(adapterPosition)
                }
            })
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (recyclerView.adapter?.getItemViewType(position)) {
                    FilmAdapter.VIEW_TYPE_HEADER -> {
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) 2
                        else 4
                    }
                    FilmAdapter.VIEW_TYPE_FILM -> 1
                    else -> 2
                }
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.onRecyclerScrolled(layoutManager.findLastVisibleItemPosition())
            }
        })


        if (position > 0 && listOfFavorite.size > position)
            recyclerView.scrollToPosition(position)

    }

    override fun onResume() {
        super.onResume()
        view?.let {
            (activity as? ActivityUpdater)?.setupBlur(requireView())
        }
    }


    interface OnFavoriteListener {
        fun onFavoriteDetail(item: FilmItem)
    }
}
package com.pavelprojects.filmlibraryproject.ui.home

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

class FilmListFragment : Fragment() {
    companion object {
        const val TAG = "FilmListFragment"
        fun newInstance() = FilmListFragment()

        const val KEY_LIST = "FilmList"
        const val KEY_RECYCLER_POS = "RecycelerViewPos"
    }

    var listOfFilms = arrayListOf<FilmItem>()
    var orientation = 0

    lateinit var layoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: FilmLibraryViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_filmlist, container, false)
        viewModel = if (activity is FilmLibraryActivity) {
            (requireActivity() as FilmLibraryActivity).viewModel
        } else {
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
                .create(FilmLibraryViewModel::class.java)
        }
        orientation = resources.configuration.orientation
        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            GridLayoutManager(requireContext(), 2)
        else
            GridLayoutManager(requireContext(), 4)
        initRecycler(view)
        initModel()
        return view
    }

    private fun initModel() {
        Log.d(TAG, "initModel")
        viewModel.getPopularMovies().observe(this.viewLifecycleOwner) {
        }
        viewModel.getAllFilms().observe(this.viewLifecycleOwner){
            if(!listOfFilms.containsAll(it))
            listOfFilms.addAll(it)
            recyclerView.adapter?.notifyDataSetChanged()
        }
        viewModel.getNetworkLoadingStatus().observe(this.viewLifecycleOwner) {

        }
        viewModel.getFavFilms().observe(this.viewLifecycleOwner){

        }

    }

    private fun initRecycler(view: View) {
        Log.d(TAG, "initRecycler")
        recyclerView = view.findViewById(R.id.recyclerView_films)
        val adapter = FilmAdapter(
            listOfFilms,
            requireContext().getString(R.string.label_library),
            viewModel,
            object : FilmAdapter.FilmClickListener() {
                override fun onDetailClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int,
                    view: View
                ) {
                    (activity as? OnFilmClickListener)?.onDetailClicked(
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
                    if (filmItem.isLiked) {
                        filmItem.isLiked = false
                        viewModel.delete(filmItem, FilmLibraryViewModel.CODE_FAV_FILM_DB)
                        //Toast.makeText(requireContext(), resources.getText(R.string.snackbar_dont_like), Toast.LENGTH_SHORT).show()
                    } else {
                        filmItem.isLiked = true
                        viewModel.insert(filmItem, FilmLibraryViewModel.CODE_FAV_FILM_DB)
                        //Toast.makeText(requireContext(), resources.getText(R.string.snackbar_like), Toast.LENGTH_SHORT).show()
                    }
                    viewModel.update(filmItem, FilmLibraryViewModel.CODE_FILM_DB)
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
                if (viewModel.getLoadingStatus() != true && App.instance.loadedPage < viewModel.allPages)
                    if (visibleItemCount + pastVisibleItem >= viewCount) {
                        viewModel.getPopularMovies()
                    }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

    }

    interface OnFilmClickListener {
        fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
    }

}
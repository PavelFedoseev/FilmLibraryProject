package com.pavelprojects.filmlibraryproject.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.*

class FilmListFragment : Fragment() {
    companion object {
        const val TAG = "FilmListFragment"
        private const val KEY_LIST_FILMS = "ListOfFilms"
        fun newInstance(listOfFilms: ArrayList<FilmItem>) = FilmListFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(KEY_LIST_FILMS, listOfFilms)
            }
        }
    }

    var listOfFilms = arrayListOf<FilmItem>()
    var orientation: Int = 0

    lateinit var layoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filmlist, container, false)
        listOfFilms =
            arguments?.getParcelableArrayList<FilmItem>(KEY_LIST_FILMS) as ArrayList<FilmItem>
        orientation = resources.configuration.orientation
        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            GridLayoutManager(requireContext(), 2)
        else
            GridLayoutManager(requireContext(), 4)
        initRecycler(view)
        return view
    }

    private fun initRecycler(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView_films)
        val adapter = FilmAdapter(
            listOfFilms,
            requireContext().getString(R.string.label_library),
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
                        (activity as? OnFilmClickListener)?.onDislikeClicked(
                            filmItem,
                            position,
                            adapterPosition
                        )
                        //Toast.makeText(requireContext(), resources.getText(R.string.snackbar_dont_like), Toast.LENGTH_SHORT).show()
                    } else {
                        filmItem.isLiked = true
                        (activity as? OnFilmClickListener)?.onLikeClicked(
                            filmItem,
                            position,
                            adapterPosition
                        )
                        //Toast.makeText(requireContext(), resources.getText(R.string.snackbar_like), Toast.LENGTH_SHORT).show()
                    }
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
    }

    interface OnFilmClickListener {
        fun onLikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun onDislikeClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun onDetailClicked(filmItem: FilmItem, position: Int, adapterPosition: Int)
    }

}
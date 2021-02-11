package com.pavelprojects.filmlibraryproject.ui.favorites

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.FilmAdapter
import com.pavelprojects.filmlibraryproject.FilmItem
import com.pavelprojects.filmlibraryproject.R


class FavoriteFilmsFragment : Fragment() {

    companion object {
        const val TAG = "Favorite Fragment"
        const val KEY_LIST_FILMS = "arrayList list films"

        fun newInstance(arrayList: ArrayList<FilmItem>) = FavoriteFilmsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(KEY_LIST_FILMS, arrayList)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private var listOfFavorite = arrayListOf<FilmItem>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_films, container, false)
        listOfFavorite = arguments?.getParcelableArrayList<FilmItem>(KEY_LIST_FILMS) as ArrayList<FilmItem>
        recyclerView = view.findViewById(R.id.recyclerView_favorite)
        initRecycler()
        return view
    }

    private fun initRecycler() {
        val orientation = resources.configuration.orientation
        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            GridLayoutManager(requireContext(), 2)
        else
            GridLayoutManager(requireContext(), 4)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = FilmAdapter(listOfFavorite, requireContext().getString(R.string.label_favorite), object : FilmAdapter.FilmClickListener() {

            override fun onDetailClick(filmItem: FilmItem, position: Int) {
                (activity as OnFavoriteListener).onFavoriteDetail(filmItem)
            }

            override fun onDoubleClick(filmItem: FilmItem, position: Int) {
                listOfFavorite.remove(filmItem)
                (activity as OnFavoriteListener).onFavoriteDeleted(filmItem)
                recyclerView.adapter?.notifyItemRemoved((recyclerView.adapter as? FilmAdapter)!!.adapterPosition)
                if (listOfFavorite.isEmpty()) {
                    recyclerView.background = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.background_recycler_favorite, null)
                }
                Toast.makeText(requireContext(), requireContext().getString(R.string.message_favorite_delete), Toast.LENGTH_SHORT).show()
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
        if (listOfFavorite.isEmpty()) {
            recyclerView.background = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.background_recycler_favorite, null)
        }
    }

    interface OnFavoriteListener {
        fun onFavoriteDeleted(item: FilmItem)
        fun onFavoriteDetail(item: FilmItem)
    }
}
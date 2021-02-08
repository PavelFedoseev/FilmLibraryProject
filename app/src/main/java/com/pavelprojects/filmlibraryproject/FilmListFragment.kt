package com.pavelprojects.filmlibraryproject

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
    var listOfLikedFilms = arrayListOf<FilmItem>()
    var orientation: Int = 0

    lateinit var layoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_filmlist, container, false)
        listOfFilms = arguments?.getParcelableArrayList<FilmItem>(KEY_LIST_FILMS) as ArrayList<FilmItem>
        orientation = resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
        layoutManager = GridLayoutManager(requireContext(), 2)
        else
            layoutManager = GridLayoutManager(requireContext(), 4)
        initListeners(view)
        initRecycler(view)
        return view
    }

    private fun initListeners(view: View) {
        view.findViewById<View>(R.id.button_favorite).setOnClickListener {
            //supportFragmentManager.beginTransaction().add(FavoriteFilmsFragment(), FavoriteFilmsFragment.TAG).commit()
        }
        view.findViewById<View>(R.id.button_invite).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.message_share))
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
    }

    private fun initRecycler(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView_films)
        val adapter = FilmAdapter(listOfFilms, object : FilmAdapter.FilmClickListener() {


            override fun onDetailClick(filmItem: FilmItem, position: Int) {
                (activity as OnFilmClickListener).onDetailClicked(filmItem, position)
            }

            override fun onDoubleClick(filmItem: FilmItem, position: Int) {
                if (filmItem.isLiked) {
                    listOfLikedFilms.remove(filmItem)
                    filmItem.isLiked = false
                    Toast.makeText(requireContext(), "Не нравится", Toast.LENGTH_SHORT).show()
                } else {
                    filmItem.isLiked = true
                    if (listOfLikedFilms.contains(filmItem))
                        listOfLikedFilms[listOfLikedFilms.indexOf(filmItem)] = filmItem
                    else
                        listOfLikedFilms.add(filmItem)
                    Toast.makeText(requireContext(), "Нравится", Toast.LENGTH_SHORT).show()
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

    interface OnFilmClickListener{
        fun onLikeClicked(filmItem: FilmItem, position: Int)
        fun onDislikeClicked(filmItem: FilmItem, position: Int)
        fun onDetailClicked(filmItem: FilmItem, position: Int)
    }

}
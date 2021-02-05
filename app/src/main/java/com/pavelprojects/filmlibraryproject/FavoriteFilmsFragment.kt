package com.pavelprojects.filmlibraryproject

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class FavoriteFilmsFragment : Fragment() {

    companion object{
        const val TAG = "Favorite Fragment"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    lateinit var recyclerView: RecyclerView
    var list_of_favorite = arrayListOf<FilmItem>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_films, container, false)
        recyclerView = view.findViewById(R.id.recyclerView_favorite)
        initRecycler()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        list_of_favorite = (activity as FilmLibraryActivity).listOfLikedFilms
    }

    private fun initRecycler(){
        recyclerView.adapter = FilmAdapter(list_of_favorite, object: FilmAdapter.FilmClickListener(){
            override fun onLikeClick(filmItem: FilmItem, position: Int) {
                //TODO("Not yet implemented")
            }

            override fun onDetailClick(filmItem: FilmItem, position: Int) {
                //FilmInfoActivity.startActivity(activity!!.parent, filmItem, position)
            }

            override fun onDoubleClick(filmItem: FilmItem, position: Int) {
                list_of_favorite.remove(filmItem)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
        })
    }
}
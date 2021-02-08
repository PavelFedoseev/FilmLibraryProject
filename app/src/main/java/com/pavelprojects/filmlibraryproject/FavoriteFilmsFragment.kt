package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView


class FavoriteFilmsFragment : Fragment() {

    companion object{
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

    lateinit var recyclerView: RecyclerView
    var list_of_favorite = arrayListOf<FilmItem>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_films, container, false)
        list_of_favorite = arguments?.getParcelableArrayList<FilmItem>(KEY_LIST_FILMS) as ArrayList<FilmItem>
        recyclerView = view.findViewById(R.id.recyclerView_favorite)
        initRecycler()
        return view
    }

    private fun initRecycler(){
        recyclerView.adapter = FilmAdapter(list_of_favorite, object: FilmAdapter.FilmClickListener(){

            override fun onDetailClick(filmItem: FilmItem, position: Int) {

            }

            override fun onDoubleClick(filmItem: FilmItem, position: Int) {
                list_of_favorite.remove(filmItem)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
        })
    }
}
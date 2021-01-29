package com.pavelprojects.filmlibraryproject

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FilmLibraryActivity : AppCompatActivity() {

    companion object {
        const val KEY_SELECTED_FILM = "selected_movie"
        const val KEY_FILM_LIST = "FILM_LIST"
    }

    var selected_film_num : Int = 0
    var list_of_films = arrayListOf<FilmItem>()

    var orientation : Int = 0

    lateinit var layoutManager : GridLayoutManager
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)
        orientation = resources.configuration.orientation

        initViews()
        findViewById<Button>(R.id.button_invite).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.message_share))
                type = "text/plain"
            }

            startActivity(Intent.createChooser(sendIntent, null))
        }

    }

    override fun onResume() {
        super.onResume()
        if(list_of_films.isEmpty()){
            list_of_films.add(FilmItem(getString(R.string.text_film_1), getString(R.string.text_description_1), R.drawable.joker, false))
            list_of_films.add(FilmItem(getString(R.string.text_film_2), getString(R.string.text_description_2), R.drawable.green_book, false))
            list_of_films.add(FilmItem(getString(R.string.text_film_3), getString(R.string.text_description_3), R.drawable.mulan, false))
            list_of_films.add(FilmItem(getString(R.string.text_film_4), getString(R.string.text_description_4), R.drawable.tenet, false))
        }

        val adapter = FilmAdapter(list_of_films, object : FilmAdapter.FilmClickListener{
            override fun onItemCLick(filmItem: FilmItem) {

            }

            override fun onLikeClick(filmItem: FilmItem) {
                //TODO("Not yet implemented")
            }

            override fun onDetailClick(filmItem: FilmItem) {
                FilmInfoActivity.startActivity(this@FilmLibraryActivity, filmItem, list_of_films.indexOf(filmItem) + 1 )// + 1 = header
            }
        })
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return when(adapter.getItemViewType(position)){
                    FilmAdapter.VIEW_TYPE_HEADER -> {
                        if(orientation == Configuration.ORIENTATION_PORTRAIT) 2
                        else 4
                    }
                    FilmAdapter.VIEW_TYPE_FILM -> 1
                    else -> 2
                }
            }
        }
        recyclerView.adapter = adapter


    }

    private fun changeTextViewColors(i: Int){

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selected_film_num = savedInstanceState.getInt(KEY_SELECTED_FILM)
        list_of_films = savedInstanceState.getParcelableArrayList<FilmItem>(KEY_FILM_LIST) as ArrayList<FilmItem>
        changeTextViewColors(selected_film_num)
    }

    private fun initViews(){
        recyclerView = findViewById(R.id.recyclerView_films)
        layoutManager = if(orientation == Configuration.ORIENTATION_PORTRAIT){
            GridLayoutManager(this, 2)
        }
        else{
            GridLayoutManager(this, 4)
        }
        recyclerView.layoutManager = layoutManager
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_FILM, selected_film_num)
        outState.putParcelableArrayList(KEY_FILM_LIST, list_of_films)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CODE_FILM_INFO -> {
                data?.let {
                    val element = data.getParcelableExtra<FilmItem>(FilmInfoActivity.TAG_FILM)
                    val position = data.getIntExtra(FilmInfoActivity.TAG_FILM_POS, 0)
                    if(element!=null){
                        list_of_films[position - 1] = element

                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_ISLIKE+list_of_films[position].isLiked)
                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_COMMENT+list_of_films[position].user_comment)
                    }

                }
            }
        }
    }
}
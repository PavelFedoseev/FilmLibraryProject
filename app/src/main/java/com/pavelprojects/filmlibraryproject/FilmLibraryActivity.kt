package com.pavelprojects.filmlibraryproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class FilmLibraryActivity : AppCompatActivity() {

    companion object {
        const val KEY_SELECTED_FILM = "selected_movie"
        const val KEY_FILM_LIST = "FILM_LIST"
        const val KEY_LIKED_FILM_LIST = "LIKED_FILM_LIST"
    }

    var listOfFilms = arrayListOf<FilmItem>()
    var listOfLikedFilms = arrayListOf<FilmItem>()

    var orientation: Int = 0

    //private val fragmentContainer by lazy { findViewById<FrameLayout>(R.id.fragmentContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)
    }

    private fun openFilmListFragment(){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FilmListFragment.newInstance(), FilmListFragment.TAG)
                .addToBackStack(null)
                .commit()
    }
    private fun openFilmInfoFragment(filmItem: FilmItem, position: Int){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FilmInfoFragment.newInstance(filmItem, position), FilmInfoFragment.TAG)
    }

    override fun onResume() {
        super.onResume()
        if (listOfFilms.isEmpty()) {
            listOfFilms.add(FilmItem(getString(R.string.text_film_1), getString(R.string.text_description_1), R.drawable.joker, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_2), getString(R.string.text_description_2), R.drawable.green_book, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_3), getString(R.string.text_description_3), R.drawable.mulan, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_4), getString(R.string.text_description_4), R.drawable.tenet, false))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        listOfFilms = savedInstanceState.getParcelableArrayList<FilmItem>(KEY_FILM_LIST) as ArrayList<FilmItem>
        listOfLikedFilms = savedInstanceState.getParcelableArrayList<FilmItem>(KEY_LIKED_FILM_LIST) as ArrayList<FilmItem>
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_FILM_LIST, listOfFilms)
        outState.putParcelableArrayList(KEY_LIKED_FILM_LIST, listOfLikedFilms)
    }
/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_FILM_INFO -> {
                data?.let {
                    val element = data.getParcelableExtra<FilmItem>(FilmInfoFragment.TAG)
                    val position = data.getIntExtra(FilmInfoFragment.TAG_FILM_POS, 0)
                    if (element != null) {
                        listOfFilms[position - 1] = element

                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_ISLIKE + listOfFilms[position - 1].isLiked)
                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_COMMENT + listOfFilms[position - 1].userComment)
                    }
                }
            }
        }
    }

 */

    override fun onBackPressed() {
        ExitDialog.createDialog(supportFragmentManager, object : ExitDialog.OnDialogClickListener {
            override fun onAcceptButtonCLick() {
                finish()
            }

            override fun onDismissButtonClick() {

            }
        })
    }
}
package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FilmLibraryActivity : AppCompatActivity(), FilmListFragment.OnFilmClickListener{

    companion object {
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

    private fun openFilmListFragment(listOfFilms: ArrayList<FilmItem>){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FilmListFragment.newInstance(listOfFilms), FilmListFragment.TAG)
                .commit()
    }
    private fun openFilmInfoFragment(filmItem: FilmItem, position: Int){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FilmInfoFragment.newInstance(filmItem, position),
                        FilmInfoFragment.TAG)
                .addToBackStack(null)
                .commit()
    }
    private fun openFavoriteFilmsFragment(favoriteFilms: ArrayList<FilmItem>){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        FavoriteFilmsFragment.newInstance(favoriteFilms),
                        FavoriteFilmsFragment.TAG)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        if (listOfFilms.isEmpty()) {
            listOfFilms.add(FilmItem(getString(R.string.text_film_1), getString(R.string.text_description_1), R.drawable.joker, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_2), getString(R.string.text_description_2), R.drawable.green_book, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_3), getString(R.string.text_description_3), R.drawable.mulan, false))
            listOfFilms.add(FilmItem(getString(R.string.text_film_4), getString(R.string.text_description_4), R.drawable.tenet, false))
        }
        openFilmListFragment(listOfFilms)
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

    override fun onLikeClicked(filmItem: FilmItem, position: Int) {
        listOfFilms[position-1] = filmItem
        listOfLikedFilms.add(filmItem)
    }

    override fun onDislikeClicked(filmItem: FilmItem, position: Int) {
        listOfFilms[position-1] = filmItem
        filmItem.isLiked = true
        listOfLikedFilms.remove(filmItem)
    }

    override fun onDetailClicked(filmItem: FilmItem, position: Int) {
        openFilmInfoFragment(filmItem, position)
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
        }
        else
        ExitDialog.createDialog(supportFragmentManager, object : ExitDialog.OnDialogClickListener {
            override fun onAcceptButtonCLick() {
                finish()
            }
            override fun onDismissButtonClick() {

            }
        })
    }
}
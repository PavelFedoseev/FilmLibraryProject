package com.pavelprojects.filmlibraryproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

class FilmLibraryActivity : AppCompatActivity() {

    companion object {
        const val KEY_SELECTED_FILM = "selected_movie"
        const val KEY_FILM_LIST = "FILM_LIST"
    }

    lateinit var button_film_1: Button
    lateinit var button_film_2: Button
    lateinit var button_film_3: Button
    lateinit var button_film_4: Button

    lateinit var textView_film_1: TextView
    lateinit var textView_film_2: TextView
    lateinit var textView_film_3: TextView
    lateinit var textView_film_4: TextView

    var selected_film_num : Int = 0
    var list_of_films = arrayListOf<FilmElement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmlibrary)

        initializeViews()



        findViewById<Button>(R.id.button_invite).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.message_share))
                type = "text/plain"
            }

            startActivity(Intent.createChooser(sendIntent, null))
        }



        button_film_1.setOnClickListener {
            selected_film_num = 1
            changeTextViewColors(selected_film_num)
            FilmInfoActivity.startActivity(this, list_of_films[0], 0)
        }
        button_film_2.setOnClickListener {
            selected_film_num = 2
            changeTextViewColors(selected_film_num)
            FilmInfoActivity.startActivity(this, list_of_films[1], 1)
        }
        button_film_3.setOnClickListener {
            selected_film_num = 3
            changeTextViewColors(selected_film_num)
            FilmInfoActivity.startActivity(this, list_of_films[2], 2)
        }
        button_film_4.setOnClickListener {
            selected_film_num = 4
            FilmInfoActivity.startActivity(this, list_of_films[3], 3)
            changeTextViewColors(selected_film_num)
        }
    }

    override fun onResume() {
        super.onResume()
        if(list_of_films.isEmpty()){
            list_of_films.add(FilmElement(getString(R.string.text_film_1), getString(R.string.text_description_1), R.drawable.joker, false))
            list_of_films.add(FilmElement(getString(R.string.text_film_2), getString(R.string.text_description_2), R.drawable.green_book, false))
            list_of_films.add(FilmElement(getString(R.string.text_film_3), getString(R.string.text_description_3), R.drawable.mulan, false))
            list_of_films.add(FilmElement(getString(R.string.text_film_4), getString(R.string.text_description_4), R.drawable.dovod, false))
        }
    }

    private fun changeTextViewColors(i: Int){
        textView_film_1.setTextColor(ResourcesCompat.getColor(resources, R.color.black,null))
        textView_film_2.setTextColor(ResourcesCompat.getColor(resources, R.color.black,null))
        textView_film_3.setTextColor(ResourcesCompat.getColor(resources, R.color.black,null))
        textView_film_4.setTextColor(ResourcesCompat.getColor(resources, R.color.black,null))

        when (i){
            1->textView_film_1.setTextColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
            2->textView_film_2.setTextColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
            3->textView_film_3.setTextColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
            4->textView_film_4.setTextColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selected_film_num = savedInstanceState.getInt(KEY_SELECTED_FILM)
        list_of_films = savedInstanceState.getParcelableArrayList<FilmElement>(KEY_FILM_LIST) as ArrayList<FilmElement>
        changeTextViewColors(selected_film_num)
    }

    private fun initializeViews(){
        button_film_1 = findViewById(R.id.button_info_1)
        button_film_2 = findViewById(R.id.button_info_2)
        button_film_3 = findViewById(R.id.button_info_3)
        button_film_4 = findViewById(R.id.button_info_4)

        textView_film_1 = findViewById(R.id.textView_name_1)
        textView_film_2 = findViewById(R.id.textView_name_2)
        textView_film_3 = findViewById(R.id.textView_name_3)
        textView_film_4 = findViewById(R.id.textView_name_4)
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
                    val element = data.getParcelableExtra<FilmElement>(FilmInfoActivity.TAG_FILM)
                    val position = data.getIntExtra(FilmInfoActivity.TAG_FILM_POS, 0)
                    if(element!=null){
                        list_of_films[position] = element
                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_ISLIKE+list_of_films[position].isLiked)
                        Log.i(TAG_ACTRES_FILMINFO, LOG_MSG_FILMINFO_COMMENT+list_of_films[position].user_comment)
                    }

                }
            }
        }
    }
}
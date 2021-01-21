package com.pavelprojects.filmlibraryproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class FilmInfoActivity : AppCompatActivity() {
    companion object{
        const val TAG_FILM = "TAG_FILM_ELEMENT"
        const val TAG_FILM_POS = "TAG_FILM_POSITION"

        fun startActivity(activity: Activity, filmElement: FilmElement, position: Int){
            Intent(activity, FilmInfoActivity::class.java).apply{
                putExtra(TAG_FILM, filmElement)
                putExtra(TAG_FILM_POS, position)
                activity.startActivityForResult(this, CODE_FILM_INFO)
            }
        }
    }
    var filmElement: FilmElement? = null
    var position = 0

    lateinit var textViewDescriprion: TextView
    lateinit var textViewName: TextView
    lateinit var editText_comment: EditText
    lateinit var buttonLike: FloatingActionButton
    lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_film_info)
        initViews()

        filmElement = intent.getParcelableExtra(TAG_FILM)
        position = intent.getIntExtra(TAG_FILM_POS, 0)
        thumbUp_Select(filmElement?.isLiked ?: true)

        textViewName.text = filmElement?.name

        textViewDescriprion.text = filmElement?.description
        editText_comment.setText(filmElement?.user_comment)

        buttonLike.setOnClickListener {

            if(filmElement?.isLiked != false){
                Snackbar.make(coordinatorLayout, getString(R.string.snackbar_dont_like), Snackbar.LENGTH_SHORT).show()
                filmElement?.isLiked = false
                buttonLike.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
            }
            else{
                Snackbar.make(coordinatorLayout, getString(R.string.snackbar_like), Snackbar.LENGTH_SHORT).show()
                filmElement?.isLiked = true
                buttonLike.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cyan, null))
            }
            thumbUp_Select(filmElement?.isLiked ?: true)
            saveResults()
        }
        editText_comment.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filmElement?.user_comment = p0?.toString()
                saveResults()
            }
        })
    }

    private fun saveResults(){
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TAG_FILM, filmElement)
            putExtra(TAG_FILM_POS, position)
        })
    }

    private fun thumbUp_Select(isLiked: Boolean){
        if(isLiked){
            buttonLike.setImageResource(R.drawable.ic_baseline_thumb_down_alt_24)
        }
        else{
            buttonLike.setImageResource(R.drawable.ic_baseline_thumb_up_alt_24)
        }
    }


    private fun initViews(){
        textViewDescriprion = findViewById(R.id.textView_description)
        textViewName = findViewById(R.id.textView_film_name)
        buttonLike = findViewById(R.id.button_like)
        coordinatorLayout = findViewById(R.id.coordinator)
        editText_comment = findViewById(R.id.editText_comment)
    }

}
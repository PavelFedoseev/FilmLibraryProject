package com.pavelprojects.filmlibraryproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class FilmInfoFragment : Fragment() {
    companion object {
        const val TAG = "FilmInfoFragment"
        const val KEY_FILMITEM = "FilmItem"
        const val KEY_POSITION = "FilmItem"
        fun newInstance(filmItem: FilmItem, position: Int) = FilmInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_FILMITEM, filmItem)
                putInt(KEY_POSITION, position)
            }
        }
    }

    var filmItem: FilmItem? = null
    private var position = 0

    private lateinit var textViewDescriprion: TextView
    private lateinit var textViewName: TextView
    private lateinit var editTextComment: EditText
    private lateinit var buttonLike: FloatingActionButton
    private lateinit var coordinatorLayout: CoordinatorLayout
    

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_film_info, container, false)

        filmItem = arguments?.getParcelable(KEY_FILMITEM)
        position = arguments?.getInt(KEY_POSITION, 0)!!

        initViews(view)
        initListeners()

        thumbUpSelect(filmItem?.isLiked ?: true)
        textViewName.text = filmItem?.name
        textViewDescriprion.text = filmItem?.description
        editTextComment.setText(filmItem?.userComment)
        return view
    }
/*
    private fun saveResults() {
            putExtra(KEY_FILMITEM, filmItem)
            putExtra(KEY_POSITION, position)
    }

 */

    private fun thumbUpSelect(isLiked: Boolean) {
        if (isLiked) {
            buttonLike.setImageResource(R.drawable.ic_baseline_thumb_down_alt_24)
        } else {
            buttonLike.setImageResource(R.drawable.ic_baseline_thumb_up_alt_24)
        }
    }

    private fun initListeners() {
        buttonLike.setOnClickListener {
            if (filmItem?.isLiked != false) {
                Snackbar.make(coordinatorLayout, getString(R.string.snackbar_dont_like), Snackbar.LENGTH_SHORT).show()
                filmItem?.isLiked = false
                buttonLike.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.teal_700, null))
            } else {
                Snackbar.make(coordinatorLayout, getString(R.string.snackbar_like), Snackbar.LENGTH_SHORT).show()
                filmItem?.isLiked = true
                buttonLike.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.cyan, null))
            }
            thumbUpSelect(filmItem?.isLiked ?: true)
                    //saveResults()
        }
        editTextComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filmItem?.userComment = p0?.toString()
                        //saveResults()
            }
        })
    }

    private fun initViews(view: View) {
        textViewDescriprion = view.findViewById(R.id.textView_description)
        textViewName = view.findViewById(R.id.textView_film_name)
        buttonLike = view.findViewById(R.id.button_like)
        coordinatorLayout = view.findViewById(R.id.coordinator)
        editTextComment = view.findViewById(R.id.editText_comment)
    }



}
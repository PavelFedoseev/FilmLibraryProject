package com.pavelprojects.filmlibraryproject.ui.info

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.FilmItem
import com.pavelprojects.filmlibraryproject.R
import no.danielzeller.blurbehindlib.BlurBehindLayout


class FilmInfoFragment : Fragment() {
    companion object {
        const val TAG = "FilmInfoFragment"
        const val KEY_FILMITEM = "FilmItem"
        fun newInstance(filmItem: FilmItem) = FilmInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_FILMITEM, filmItem)
            }
        }
    }

    var filmItem: FilmItem? = null

    private lateinit var textViewDescriprion: TextView
    private lateinit var editTextComment: EditText
    private lateinit var buttonLike: FloatingActionButton
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var imageViewPreview: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarLayout: CollapsingToolbarLayout

    private lateinit var blurLayout: BlurBehindLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_film_info, container, false)
        filmItem = arguments?.getParcelable(KEY_FILMITEM)

        initViews(view)
        initListeners()

        thumbUpSelect(filmItem?.isLiked ?: true)
        toolbar.title = filmItem?.name
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        //toolbarLayout.title = filmItem?.name

        textViewDescriprion.text = filmItem?.description
        editTextComment.setText(filmItem?.userComment)
        val iconId = filmItem?.icon_id ?: ResourcesCompat.getColor(resources, R.color.gray, null)
        imageViewPreview.setImageResource(iconId)
        return view
    }

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
                Snackbar.make(
                    coordinatorLayout,
                    getString(R.string.snackbar_dont_like),
                    Snackbar.LENGTH_SHORT
                ).show()
                filmItem?.isLiked = false
                buttonLike.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.orange_700,
                        null
                    )
                )
            } else {
                Snackbar.make(
                    coordinatorLayout,
                    getString(R.string.snackbar_like),
                    Snackbar.LENGTH_SHORT
                ).show()
                filmItem?.isLiked = true
                buttonLike.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.cyan,
                        null
                    )
                )
            }
            thumbUpSelect(filmItem?.isLiked ?: true)
            //saveResults()
            filmItem?.let { it1 -> (activity as OnInfoFragmentListener).onRateButtonClicked(it1) }
        }
        editTextComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filmItem?.userComment = p0?.toString()
            }
        })
    }

    private fun initViews(view: View) {
        textViewDescriprion = view.findViewById(R.id.textView_description)
        buttonLike = view.findViewById(R.id.button_like)
        coordinatorLayout = view.findViewById(R.id.coordinator)
        editTextComment = view.findViewById(R.id.editText_comment)
        imageViewPreview = view.findViewById(R.id.imageView_preview)
        toolbar = view.findViewById(R.id.toolbar)
        blurLayout = view.findViewById(R.id.blurBehindLayout)
        blurLayout.viewBehind = imageViewPreview
        toolbarLayout = view.findViewById(R.id.collapsing_toolbar)
    }

    interface OnInfoFragmentListener {
        fun onRateButtonClicked(item: FilmItem)
    }


}
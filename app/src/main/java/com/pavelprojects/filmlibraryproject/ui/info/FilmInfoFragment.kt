package com.pavelprojects.filmlibraryproject.ui.info

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pavelprojects.filmlibraryproject.LINK_TMDB_POSTER
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.ui.ActivityUpdater
import com.pavelprojects.filmlibraryproject.ui.home.FilmListFragment
import no.danielzeller.blurbehindlib.BlurBehindLayout


class FilmInfoFragment : Fragment() {
    companion object {
        const val TAG = "FilmInfoFragment"
        const val KEY_FILMITEM = "FilmItem"
        const val KEY_FRAGMENT_TAG = "FragmentType"
        fun newInstance(filmItem: FilmItem, fragmentTag: String) = FilmInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_FILMITEM, filmItem)
                putString(KEY_FRAGMENT_TAG, fragmentTag)
            }
        }
    }

    var filmItem: FilmItem? = null
    private lateinit var callerFragmentTag: String

    private lateinit var textViewDescriprion: TextView
    private lateinit var editTextComment: EditText
    private lateinit var buttonLike: FloatingActionButton
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var imageViewPreview: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarLayout: CollapsingToolbarLayout
    private lateinit var checkBoxWatchLater: CheckBox
    private lateinit var progressBarRating: ProgressBar
    private lateinit var textViewRating: TextView

    private lateinit var blurLayout: BlurBehindLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation =
                TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
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
        callerFragmentTag = arguments?.getString(KEY_FRAGMENT_TAG, FilmListFragment.TAG)
                ?: FilmListFragment.TAG
        initViews(view)
        initListeners()
        thumbUpSelect(filmItem?.isLiked ?: true)
        toolbar.title = filmItem?.name
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        textViewDescriprion.text = filmItem?.description
        editTextComment.setText(filmItem?.userComment)
        Glide.with(this)
                .load(LINK_TMDB_POSTER + filmItem?.backdropPath)
                .transform(CenterCrop())
                .into(imageViewPreview)
        (activity as? ActivityUpdater)?.hideAppBar()
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
                (activity as? ActivityUpdater)?.makeSnackBar(getString(R.string.snackbar_dont_like), Snackbar.LENGTH_SHORT)
                filmItem?.isLiked = false
                buttonLike.setBackgroundColor(
                        ResourcesCompat.getColor(
                                resources,
                                R.color.orange_700,
                                null
                        )
                )
            } else {
                (activity as? ActivityUpdater)?.makeSnackBar(getString(R.string.snackbar_like), Snackbar.LENGTH_SHORT)
                filmItem?.isLiked = true
                buttonLike.setBackgroundColor(
                        ResourcesCompat.getColor(
                                resources,
                                R.color.cyan,
                                null
                        )
                )
            }
            filmItem?.let { it1 -> (activity as? OnInfoFragmentListener)?.onRateButtonClicked(it1, callerFragmentTag) }
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
            }
        })
        checkBoxWatchLater.setOnCheckedChangeListener { compoundButton, _ ->
            filmItem?.isWatchLater = compoundButton.isChecked
            if(compoundButton.isChecked){
                (activity as? ActivityUpdater)?.makeSnackBar(getString(R.string.snackbar_watchlater_added), Snackbar.LENGTH_SHORT)
            }
            else{
                (activity as? ActivityUpdater)?.makeSnackBar(getString(R.string.snackbar_watchlater_removed), Snackbar.LENGTH_SHORT)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val rating = filmItem?.rating ?: 0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBarRating.setProgress(rating.toInt()*10, true)
        }
        else progressBarRating.progress = rating.toInt()*10
        textViewRating.text = rating.toString()
    }

    override fun onDestroy() {
        filmItem?.let { it1 -> (activity as? OnInfoFragmentListener)?.onRateButtonClicked(it1, callerFragmentTag) }
        super.onDestroy()
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
        checkBoxWatchLater = view.findViewById(R.id.checkbox_watch_later)
        progressBarRating = view.findViewById(R.id.progress_bar_rating)
        textViewRating = view.findViewById(R.id.text_view_rating)

        if(filmItem?.isWatchLater == true){
            checkBoxWatchLater.isChecked = true
        }
    }

    interface OnInfoFragmentListener {
        fun onRateButtonClicked(item: FilmItem, fragmentTag: String)
    }


}
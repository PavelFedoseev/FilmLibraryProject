package com.pavelprojects.filmlibraryproject.ui.watchlater

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.App
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toFilmItem
import com.pavelprojects.filmlibraryproject.ui.ActivityUpdater
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryViewModel
import com.pavelprojects.filmlibraryproject.ui.LibraryActivityChild
import com.pavelprojects.filmlibraryproject.ui.info.FilmInfoFragment
import java.util.*


class WatchLaterFragment : Fragment(), LibraryActivityChild {

    companion object {
        const val TAG = "WatchLatter Tag"
        fun newInstance() = WatchLaterFragment()
    }

    private lateinit var viewModel: FilmLibraryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var listOfWatchLater: ArrayList<ChangedFilmItem>

    private var position: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_favorite_films, container, false)
        listOfWatchLater = arrayListOf()
        recyclerView = view.findViewById(R.id.recyclerView_favorite)
        position = App.instance.recFavPos
        initModel()
        initRecycler(position)
        (activity as? ActivityUpdater)?.setupBlur(view)
        return view
    }

    private fun initModel() {
        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(FilmLibraryViewModel::class.java)

        viewModel.getWatchLatter().observe(this.viewLifecycleOwner) {
            listOfWatchLater.clear()
            listOfWatchLater.addAll(it)
            recyclerView.adapter?.notifyDataSetChanged()
            (activity as? ActivityUpdater)?.updateNotificationChannel(requireContext(), it)
            if (listOfWatchLater.isEmpty()) {
                recyclerView.background = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.background_recycler_favorite,
                    null
                )
            } else recyclerView.background = null
        }
    }


    private fun initRecycler(position: Int = 0) {
        val orientation = resources.configuration.orientation
        layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 2
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = WatchLaterAdapter(
            listOfWatchLater,
            requireContext().getString(R.string.label_watch_later),
            object : WatchLaterAdapter.FilmClickListener() {

                override fun onDetailClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int,
                    view: View
                ) {
                    //val extras = FragmentNavigatorExtras(view to "imageview_film_info")
                    (activity as? OnWatchLaterListener)?.onWatchLaterDetail(filmItem)
                }

                override fun onDoubleClick(
                    filmItem: FilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {

                }

                override fun onReminderClick(
                    changedFilmItem: ChangedFilmItem,
                    position: Int,
                    adapterPosition: Int
                ) {
                    createDatePickerDialog(changedFilmItem)
                }
            })


        if (position > 0 && listOfWatchLater.size > position)
            recyclerView.scrollToPosition(position)
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder.adapterPosition != 0) {
                    listOfWatchLater[viewHolder.adapterPosition - 1].isWatchLater = false
                    (activity as? FilmInfoFragment.OnInfoFragmentListener)?.onRateButtonClicked(
                        listOfWatchLater[viewHolder.adapterPosition - 1].toFilmItem(),
                        TAG
                    )
                    listOfWatchLater.removeAt(viewHolder.adapterPosition - 1)
                    recyclerView.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }
                if (listOfWatchLater.isEmpty()) {
                    recyclerView.background = ResourcesCompat.getDrawable(
                        requireContext().resources,
                        R.drawable.background_recycler_favorite,
                        null
                    )
                } else recyclerView.background = null
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
        }
        ItemTouchHelper(simpleCallback).apply { attachToRecyclerView(recyclerView) }

    }

    private fun createDatePickerDialog(changedFilmItem: ChangedFilmItem){
        val calendar = Calendar.getInstance()
        val position = listOfWatchLater.indexOf(changedFilmItem)
        DatePickerDialog(
            requireContext(),
            { p0, p1, p2, p3 ->
                calendar.set(Calendar.YEAR, p1)
                calendar.set(Calendar.MONTH, p2)
                calendar.set(Calendar.DAY_OF_MONTH, p3)
                TimePickerDialog(requireContext(), { timePicker: TimePicker, i: Int, i1: Int ->
                    calendar.set(Calendar.HOUR_OF_DAY, i)
                    calendar.set(Calendar.MINUTE, i1)
                    calendar.add(Calendar.SECOND, 5)
                    changedFilmItem.watchLaterDate = calendar.timeInMillis
                    listOfWatchLater[position] = changedFilmItem
                    recyclerView.adapter?.notifyItemChanged(position + 1) //+1 Header
                    viewModel.updateChanged(changedFilmItem)
                    (activity as? ActivityUpdater)?.updateNotificationChannel(requireContext(), listOfWatchLater)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply { this.datePicker.minDate = calendar.timeInMillis }.show()

    }

    override fun onOnlineStatusChanged(isOnline: Boolean) {
    }

    interface OnWatchLaterListener {
        fun onWatchLaterDetail(item: FilmItem)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        App.instance.recFavPos = layoutManager.findLastVisibleItemPosition()
    }

    override fun onDestroy() {
        App.instance.recFavPos = layoutManager.findLastVisibleItemPosition()
        super.onDestroy()
    }
}
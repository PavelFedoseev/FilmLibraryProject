package com.pavelprojects.filmlibraryproject.ui.watchlater

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.toFilmItem
import com.pavelprojects.filmlibraryproject.network.RetroApi
import com.pavelprojects.filmlibraryproject.ui.FilmAdapter
import java.text.SimpleDateFormat
import java.util.*

class WatchLaterAdapter(
    var list: List<ChangedFilmItem>,
    var header: String,
    var listener: FilmClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TAG = "WatchLaterAdapter"
        const val VIEW_TYPE_FOOTER = 0
        const val VIEW_TYPE_FILM = 1
        const val VIEW_TYPE_HEADER = 2
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            VIEW_TYPE_FILM -> {
                view = layoutInflater.inflate(R.layout.item_watch_later_element, parent, false)
                FilmItemViewHolder(view)
            }
            VIEW_TYPE_HEADER -> {
                view = layoutInflater.inflate(R.layout.item_header, parent, false)
                HeaderItemViewHolder(view, header)
            }
            VIEW_TYPE_FOOTER -> {
                view = layoutInflater.inflate(R.layout.item_footer, parent, false)
                FooterItemViewHolder(view)
            }
            else -> { //VIEW_TYPE_NULL
                view = layoutInflater.inflate(R.layout.item_null, parent, false)
                EmptyItemViewHolder(view)
            }
        }

    }

    override fun getItemCount(): Int = list.size + 2 // + 2 = Header + Footer

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FilmItemViewHolder) {
            val item = list[position - 1]
            holder.bindView(item)
            holder.itemView.setOnClickListener {
                listener.onDetailClick(
                    item.toFilmItem(),
                    position,
                    holder.adapterPosition,
                    holder.itemView
                )
            }
            holder.remindImageButton.setOnClickListener {
                listener.onReminderClick(item, position, holder.adapterPosition)
            }
        } else if (holder is HeaderItemViewHolder) {
            holder.bindView()
        } else if (holder is FooterItemViewHolder) {
            holder.bindView()
        }
    }

    fun setValue(list: List<ChangedFilmItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            list.size + 1 -> VIEW_TYPE_FOOTER
            else -> VIEW_TYPE_FILM
        }
    }

    class FilmItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTv: TextView = itemView.findViewById(R.id.textView_name)
        val remindImageButton: ImageButton = itemView.findViewById(R.id.imagebtn_reminder)
        val remindTextView: TextView = itemView.findViewById(R.id.textView_remind_date)
        var item: ChangedFilmItem? = null
        fun bindView(item: ChangedFilmItem) {
            this.item = item
            titleTv.text = item.name
            if (item.watchLaterDate != -1L) {
                val calendar = Calendar.getInstance().apply { timeInMillis = item.watchLaterDate }
                remindTextView.text =
                    SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH).format(calendar.time)
                if(calendar.timeInMillis < Calendar.getInstance().timeInMillis)
                    remindTextView.setTextColor(itemView.resources.getColor(R.color.red, null))
                else
                    remindTextView.setTextColor(itemView.resources.getColor(android.R.color.darker_gray, null))
            }

            Glide.with(itemView)
                .load(RetroApi.BASE_URL_POSTER + item.posterPath)
                .transform()
                .into(imageView)
        }
    }

    class FooterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progrss_bar_loading)
        fun bindView() {
            progressBar.visibility = View.GONE
            Log.d(FilmAdapter.TAG, "FooterItemViewHolder: bindView")
        }
    }

    class HeaderItemViewHolder(itemView: View, var header: String) :
        RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textview_header)
        fun bindView() {
            textView.text = header
        }
    }

    class EmptyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface FilmClickInterface {
        fun onItemClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun onDetailClick(filmItem: FilmItem, position: Int, adapterPosition: Int, view: View)
        fun onReminderClick(changedFilmItem: ChangedFilmItem, position: Int, adapterPosition: Int)
    }


    abstract class FilmClickListener : FilmClickInterface {
        companion object {
            private const val DOUBLE_CLICK_DELTA = 300 //milleseconds interaval between clicks
        }

        private var lastClickTime: Long = 0
        override fun onItemClick(filmItem: FilmItem, position: Int, adapterPosition: Int) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < DOUBLE_CLICK_DELTA) {
                onDoubleClick(filmItem, position, adapterPosition)
            }
            lastClickTime = currentTime
        }

        abstract fun onDoubleClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
    }
}

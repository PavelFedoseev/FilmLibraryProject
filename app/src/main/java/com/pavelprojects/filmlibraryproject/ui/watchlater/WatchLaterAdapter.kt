package com.pavelprojects.filmlibraryproject.ui.watchlater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.ui.FilmLibraryViewModel

class WatchLaterAdapter(
    var list: List<FilmItem>,
    var header: String,
    var listener: FilmClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TAG = "WatchLaterAdapter"
        const val VIEW_TYPE_FILM = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_NULL = -1
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
            else -> { //VIEW_TYPE_NULL
                view = layoutInflater.inflate(R.layout.item_null, parent, false)
                EmptyItemViewHolder(view)
            }
        }


    }

    override fun getItemCount(): Int = list.size + 1 // + 2 = Header + Footer

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FilmItemViewHolder) {
            val item = list[position - 1]
            holder.bindView(item)
            holder.itemView.setOnClickListener {
                listener.onItemClick(item, position, holder.adapterPosition)
            }
            holder.detailButton.setOnClickListener {
                listener.onDetailClick(item, position, holder.adapterPosition, holder.itemView)
            }
            holder.titleTv.setOnClickListener {
                listener.onItemClick(item, position, holder.adapterPosition)
            }
            holder.remindImageButton.setOnClickListener {
                listener.onReminderClick(item, position, holder.adapterPosition)
            }
        } else if (holder is HeaderItemViewHolder) {
            holder.bindView()
        }
    }

    fun setValue(list: List<FilmItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_FILM
        }
    }

    class FilmItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTv: TextView = itemView.findViewById(R.id.textView_name)
        val detailButton: Button = itemView.findViewById(R.id.button_detail)
        val remindImageButton: ImageButton = itemView.findViewById(R.id.imagebtn_reminder)
        var item: FilmItem? = null
        fun bindView(item: FilmItem) {
            this.item = item
            titleTv.text = item.name

            Glide.with(itemView)
                .load("https://image.tmdb.org/t/p/w342${item.posterPath}")
                .transform()
                .into(imageView)
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
        fun onReminderClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
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

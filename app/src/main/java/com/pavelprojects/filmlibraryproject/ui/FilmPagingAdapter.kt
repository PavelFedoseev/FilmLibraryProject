package com.pavelprojects.filmlibraryproject.ui

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pavelprojects.filmlibraryproject.R
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.network.RetroApi

class FilmPagingAdapter(
    var header: String,
    var listener: FilmClickListener
) : PagingDataAdapter<FilmItem, RecyclerView.ViewHolder>(
    COMPARATOR
) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<FilmItem>() {
            override fun areItemsTheSame(oldItem: FilmItem, newItem: FilmItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FilmItem, newItem: FilmItem): Boolean {
                return oldItem == newItem
            }
        }
        const val VIEW_TYPE_FILM = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_FOOTER = 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { filmItem ->
            when (holder) {
                is FilmItemViewHolder -> {
                    holder.bindView(filmItem, position, false)
                    holder.itemView.setOnClickListener {
                        listener.onItemClick(filmItem, position, holder.bindingAdapterPosition)
                    }
                }
                is HeaderItemViewHolder -> {
                    holder.bindView()
                }
                is FooterItemViewHolder -> {
                    holder.bindView()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            itemCount + 1 -> VIEW_TYPE_FOOTER
            else -> VIEW_TYPE_FILM
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            VIEW_TYPE_FILM -> {
                view = layoutInflater.inflate(R.layout.item_filmelement, parent, false)
                FilmItemViewHolder(view)
            }
            FilmAdapter.VIEW_TYPE_FOOTER -> {
                view = layoutInflater.inflate(R.layout.item_footer, parent, false)
                FooterItemViewHolder(view)
            }
            FilmAdapter.VIEW_TYPE_HEADER -> {
                view = layoutInflater.inflate(R.layout.item_header, parent, false)
                HeaderItemViewHolder(view, header)
            }
            else -> { //VIEW_TYPE_NULL
                view = layoutInflater.inflate(R.layout.item_null, parent, false)
                EmptyItemViewHolder(view)
            }
        }
    }


    class HeaderItemViewHolder(itemView: View, var header: String) :
        RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textview_header)
        fun bindView() {
            textView.text = header
        }
    }

    class FilmItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewLike: ImageView = itemView.findViewById(R.id.imageView_like)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTv: TextView = itemView.findViewById(R.id.textView_name)
        var item: FilmItem? = null

        fun bindView(item: FilmItem, position: Int, isAddRotation: Boolean) {
            this.item = item
            titleTv.text = item.name
            if (isAddRotation)
                if (position % 2 != 0)
                    itemView.rotationY =
                        itemView.context.resources.getDimension(R.dimen.filmitem_rotation)
                else
                    itemView.rotationY =
                        -itemView.context.resources.getDimension(R.dimen.filmitem_rotation)
            Glide.with(itemView)
                .load(RetroApi.BASE_URL_POSTER + item.posterPath)
                .transform()
                .into(imageView)
        }

    }

    class FooterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progrss_bar_loading)
        fun bindView() {
            Log.d(FilmAdapter.TAG, "FooterItemViewHolder: bindView")
            progressBar.visibility = View.GONE
        }
    }


    class EmptyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface FilmClickInterface {
        fun onItemClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
        fun onDetailClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
    }


    abstract class FilmClickListener : FilmClickInterface {
        companion object {
            private const val DOUBLE_CLICK_DELTA = 200 //milleseconds interaval between clicks
        }

        private var lastClickTime: Long = 0
        private var handler = Handler(Looper.getMainLooper())
        override fun onItemClick(filmItem: FilmItem, position: Int, adapterPosition: Int) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < DOUBLE_CLICK_DELTA) {
                handler.removeCallbacksAndMessages(null)
                onDoubleClick(filmItem, position, adapterPosition)
            } else {
                handler.postDelayed(
                    { onDetailClick(filmItem, position, adapterPosition) },
                    DOUBLE_CLICK_DELTA.toLong()
                )
            }
            lastClickTime = currentTime
        }

        override fun onDetailClick(filmItem: FilmItem, position: Int, adapterPosition: Int) {

        }

        abstract fun onDoubleClick(filmItem: FilmItem, position: Int, adapterPosition: Int)
    }
}
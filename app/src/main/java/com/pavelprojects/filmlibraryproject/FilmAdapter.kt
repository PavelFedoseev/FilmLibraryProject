package com.pavelprojects.filmlibraryproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FilmAdapter(var list: List<FilmItem>, var listener: FilmClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_FOOTER = 0
        const val VIEW_TYPE_FILM = 1
        const val VIEW_TYPE_HEADER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            VIEW_TYPE_FILM -> {
                view = layoutInflater.inflate(R.layout.item_filmelement, parent, false)
                FilmItemViewHolder(view)
            }
            VIEW_TYPE_FOOTER -> {
                view = layoutInflater.inflate(R.layout.item_footer, parent, false)
                FooterItemViewHolder(view)
            }
            else -> {
                view = layoutInflater.inflate(R.layout.item_header, parent, false)
                HeaderItemViewHolder(view)
            }
        }


    }

    override fun getItemCount(): Int = list.size + 2 // + 2 = Header + Footer

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FilmItemViewHolder) {
            val item = list[position - 1]
            holder.bindView(item)
            holder.itemView.setOnClickListener {
                listener.onItemCLick(item, position)
            }
            holder.detailButton.setOnClickListener {
                listener.onDetailClick(item, position)
            }
            holder.titleTv.setOnClickListener {
                listener.onItemCLick(item, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            list.size + 1 -> VIEW_TYPE_FOOTER
            else -> VIEW_TYPE_FILM
        }
    }

    class FilmItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewLike: ImageView = itemView.findViewById(R.id.imageView_like)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTv: TextView = itemView.findViewById(R.id.textView_name)
        val detailButton: Button = itemView.findViewById(R.id.button_detail)
        var item: FilmItem? = null
        fun bindView(item: FilmItem) {
            this.item = item
            imageView.setImageResource(item.icon_id)
            titleTv.text = item.name
        }
    }

    class FooterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO Footer
        fun bindView() {}
    }

    class HeaderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO Header
        fun bindView() {}
    }

    interface FilmClickInterface {
        fun onItemCLick(filmItem: FilmItem, position: Int)
        fun onLikeClick(filmItem: FilmItem, position: Int)
        fun onDetailClick(filmItem: FilmItem, position: Int)
    }

    abstract class FilmClickListener : FilmClickInterface {
        companion object {
            private const val DOUBLE_CLICK_DELTA = 300 //milleseconds interaval between clicks
        }
        private var lastClickTime: Long = 0
        override fun onItemCLick(filmItem: FilmItem, position: Int) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < DOUBLE_CLICK_DELTA) {
                onDoubleClick(filmItem, position)
            }
            lastClickTime = currentTime
        }
        abstract fun onDoubleClick(filmItem: FilmItem, position: Int)
    }
}

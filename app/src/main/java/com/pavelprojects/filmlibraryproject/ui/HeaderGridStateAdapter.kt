package com.pavelprojects.filmlibraryproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.R

class HeaderGridStateAdapter(val headerTitle: String) : LoadStateAdapter<HeaderGridStateAdapter.HeaderItemViewHolder>() {

    override fun onBindViewHolder(holder: HeaderItemViewHolder, loadState: LoadState) {
        holder.bindView()
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): HeaderItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HeaderItemViewHolder(
            layoutInflater.inflate(
                R.layout.item_header,
                parent,
                false
            ),
            headerTitle
        )
    }

    class HeaderItemViewHolder(itemView: View, var header: String) :
        RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textview_header)
        fun bindView() {
            textView.text = header
        }
    }
}
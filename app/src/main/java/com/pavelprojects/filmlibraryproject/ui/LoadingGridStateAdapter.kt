package com.pavelprojects.filmlibraryproject.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavelprojects.filmlibraryproject.R

class LoadingGridStateAdapter : LoadStateAdapter<LoadingGridStateAdapter.FooterItemViewHolder>() {

    override fun onBindViewHolder(holder: FooterItemViewHolder, loadState: LoadState) {
        holder.bindView()
    }




    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): FooterItemViewHolder  {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FooterItemViewHolder(layoutInflater.inflate(R.layout.item_footer, parent, false))
    }

    class FooterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progrss_bar_loading)
        fun bindView() {
            Log.d(FilmAdapter.TAG, "FooterItemViewHolder: bindView")
            progressBar.visibility = View.VISIBLE
        }
    }
}
package com.pavelprojects.filmlibraryproject.ui

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FavFilmItemAnimator(val context: Context) : DefaultItemAnimator() {
    override fun animateMove(
        holder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {

        return super.animateMove(holder, fromX, fromY, toX, toY)
    }


    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: MutableList<Any>) = true
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = true
}
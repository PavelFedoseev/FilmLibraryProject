package com.pavelprojects.filmlibraryproject

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FilmItemAnimator(val context: Context) : DefaultItemAnimator() {
    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, preInfo: ItemHolderInfo, postInfo: ItemHolderInfo): Boolean {

        val holder = newHolder as FilmAdapter.FilmItemViewHolder

        if (preInfo is FilmItemHolderInfo) {
            if (preInfo.wasLiked) {
                animateLikeIcon(holder, R.drawable.ic_baseline_thumb_up_alt_24)
            } else {
                animateLikeIcon(holder, R.drawable.ic_baseline_thumb_down_alt_24)
            }
            return true
        }

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    override fun recordPreLayoutInformation(state: RecyclerView.State, viewHolder: RecyclerView.ViewHolder, changeFlags: Int, payloads: MutableList<Any>): ItemHolderInfo {
        if (changeFlags == FLAG_CHANGED)
            for (payload in payloads)
                if (payload as? String == TAG_LIKE_ANIM) {
                    return FilmItemHolderInfo((viewHolder as? FilmAdapter.FilmItemViewHolder)?.item!!.isLiked)
                }
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }

    private fun animateLikeIcon(holder: FilmAdapter.FilmItemViewHolder, resId: Int) {
        holder.imageViewLike.visibility = View.VISIBLE
        holder.imageViewLike.setImageResource(resId)

        val animationIn = AnimationUtils.loadAnimation(context, R.anim.anim_like_1)
        val animationOut = AnimationUtils.loadAnimation(context, R.anim.anim_like_2)
        animationIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                holder.imageViewLike.startAnimation(animationOut)
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
        animationOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                holder.imageViewLike.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
        holder.imageViewLike.startAnimation(animationIn)
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: MutableList<Any>) = true
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = true

    class FilmItemHolderInfo(val wasLiked: Boolean) : ItemHolderInfo()

}

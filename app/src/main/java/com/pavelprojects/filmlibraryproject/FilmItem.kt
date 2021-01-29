package com.pavelprojects.filmlibraryproject

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FilmItem(var name: String, var description: String, var icon_id: Int, var isLiked: Boolean, var isSelected: Boolean = false, var isEdit: Boolean = false, var user_comment: String? = null) : Parcelable
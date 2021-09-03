package com.pavelprojects.filmlibraryproject.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "changed_film_table")
@Parcelize
data class ChangedFilmItem(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var name: String?,
    var description: String?,
    var posterPath: String?,
    var backdropPath: String?,
    var rating: Float?,
    var releaseDate: String?,
    var userComment: String? = null,
    var isLiked: Boolean = false,
    @ColumnInfo(name ="isWatchLater")var isWatchLater: Boolean = false,
    @ColumnInfo(name ="watchLatterDate")var watchLaterDate: Long = -1
) : Parcelable

fun ChangedFilmItem.toFilmItem() = FilmItem(
    id,
    name,
    description,
    posterPath,
    backdropPath,
    rating,
    releaseDate,
    userComment,
    isLiked,
    isWatchLater
)
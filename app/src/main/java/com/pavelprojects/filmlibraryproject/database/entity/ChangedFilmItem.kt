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
    @PrimaryKey(autoGenerate = true)
    var id: Int = -1,
    @ColumnInfo(name = "film_id") var filmId: Int?, //TMDB film id
    var name: String?,
    var description: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val rating: Float?,
    val releaseDate: String?,
    var userComment: String? = null,
    var isLiked: Boolean = false,
    @ColumnInfo(name ="isWatchLater")var isWatchLater: Boolean = false,
    @ColumnInfo(name ="watchLatterDate")var watchLaterDate: Long = -1
) : Parcelable

fun ChangedFilmItem.toFilmItem() = FilmItem(
    0,
    filmId,
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
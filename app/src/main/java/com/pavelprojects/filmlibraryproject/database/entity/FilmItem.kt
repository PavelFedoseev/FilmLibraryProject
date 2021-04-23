package com.pavelprojects.filmlibraryproject.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "film_table")
@Parcelize
data class FilmItem(
        @ColumnInfo(name = "id")
        @SerializedName("current_id")
        @PrimaryKey(autoGenerate = true)
        var id: Int = -1,
        @SerializedName("id") @ColumnInfo(name = "film_id") var filmId: Int?, //TMDB film id
        @SerializedName("title") var name: String?,
        @SerializedName("overview") var description: String?,
        @SerializedName("poster_path") val posterPath: String?,
        @SerializedName("backdrop_path") val backdropPath: String?,
        @SerializedName("vote_average") val rating: Float?,
        @SerializedName("release_date") val releaseDate: String?,
        var userComment: String? = null,
        var isLiked: Boolean = false,
        @ColumnInfo(name ="isWatchLater")var isWatchLater: Boolean = false
) : Parcelable

fun FilmItem.toChangedFilmItem() = ChangedFilmItem(
        id,
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
package com.pavelprojects.filmlibraryproject.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ResponseItem(
        @SerializedName("id")var id: Int,
        @SerializedName("title") var name: String?,
        @SerializedName("overview") var description: String?,
        @SerializedName("poster_path") val posterPath: String?,
        @SerializedName("backdrop_path") val backdropPath: String?,
        @SerializedName("vote_average") val rating: Float?,
        @SerializedName("release_date") val releaseDate: String?,
) : Parcelable
fun ResponseItem.toFilmItem() = FilmItem(
        id = id,
        name = name,
        description = description,
        posterPath = posterPath,
        backdropPath = backdropPath,
        rating = rating,
        releaseDate = releaseDate
)
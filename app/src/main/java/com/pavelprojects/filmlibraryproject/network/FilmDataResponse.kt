package com.pavelprojects.filmlibraryproject.network

import com.google.gson.annotations.SerializedName
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem

data class FilmDataResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val movies: List<ResponseItem>,
    @SerializedName("total_pages") val pages: Int
)
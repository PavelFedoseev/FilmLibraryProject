package com.pavelprojects.filmlibraryproject.retrofit

import com.google.gson.annotations.SerializedName
import com.pavelprojects.filmlibraryproject.FilmItem

data class FilmDataResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val movies: List<FilmItem>,
    @SerializedName("total_pages") val pages: Int
)
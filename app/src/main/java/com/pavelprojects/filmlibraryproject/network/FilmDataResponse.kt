package com.pavelprojects.filmlibraryproject.network

import com.google.gson.annotations.SerializedName
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem

class FilmDataResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val films: List<ResponseItem>,
    @SerializedName("total_pages") val totalPages: Int
)
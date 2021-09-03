package com.pavelprojects.filmlibraryproject.domain.extentions

import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

fun List<FilmItem>.compare(list: List<FilmItem>): List<FilmItem>{
    return filter {
        var isNotContains = true
        list.forEach {   if(it.id == it.id) isNotContains = false }
        isNotContains
    }
}
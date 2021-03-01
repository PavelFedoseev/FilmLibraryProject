package com.pavelprojects.filmlibraryproject.dao

import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.pavelprojects.filmlibraryproject.FilmItem

@Dao
interface FilmItemDao {

    @Insert
    fun insert(filmItem: FilmItem)

    @Insert
    fun insertAll(filmList: List<FilmItem>)

    @Update
    fun update(filmItem: FilmItem)

    @Delete
    fun delete(filmItem: FilmItem)

    @Query("DELETE FROM film_table")
    fun deleteAllFilms()

    @Query("SELECT * FROM film_table")
    fun getAll(): List<FilmItem>

    @Query("SELECT * FROM film_table WHERE id = :id")
    fun getById(id: Long): FilmItem?
}
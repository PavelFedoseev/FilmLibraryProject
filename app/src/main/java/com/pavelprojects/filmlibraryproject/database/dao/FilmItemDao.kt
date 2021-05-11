package com.pavelprojects.filmlibraryproject.database.dao

import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

@Dao
interface FilmItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filmItem: FilmItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
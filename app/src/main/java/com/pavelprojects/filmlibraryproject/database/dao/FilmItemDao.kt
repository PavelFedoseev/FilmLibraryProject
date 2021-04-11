package com.pavelprojects.filmlibraryproject.database.dao

import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

@Dao
interface FilmItemDao {

    @Insert
    fun insert(filmItem: FilmItem)

    @Insert
    fun insert(changedFilmItem: ChangedFilmItem)

    @Insert
    fun insertAll(filmList: List<FilmItem>)

    @Insert
    fun insertAllFav(changedFilmList: List<ChangedFilmItem>)

    @Update
    fun update(filmItem: FilmItem)

    @Update
    fun update(changedFilmItem: ChangedFilmItem)

    @Delete
    fun delete(filmItem: FilmItem)

    @Delete(entity = ChangedFilmItem::class)
    fun delete(changedFilmItem: ChangedFilmItem)

    @Query("DELETE FROM film_table")
    fun deleteAllFilms()

    @Query("DELETE FROM changed_film_table")
    fun deleteAllFavFilms()

    @Query("SELECT * FROM film_table")
    fun getAll(): List<FilmItem>

    @Query("SELECT * FROM changed_film_table")
    fun getAllFav(): List<FilmItem>

    @Query("SELECT * FROM film_table WHERE id = :id")
    fun getById(id: Long): FilmItem?
}
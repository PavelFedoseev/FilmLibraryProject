package com.pavelprojects.filmlibraryproject.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItemRemoteKeys
import io.reactivex.Maybe

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
    fun clearFilms()

    @Query("SELECT * FROM film_table")
    fun getAll(): Maybe<List<FilmItem>>

    @Query("SELECT * FROM film_table WHERE id = :id")
    fun getById(id: Int): Maybe<FilmItem>
}
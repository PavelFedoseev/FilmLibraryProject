package com.pavelprojects.filmlibraryproject.database.dao

import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

@Dao
interface ChangedItemDao {

    @Insert
    fun insert(changedFilmItem: ChangedFilmItem)

    @Insert
    fun insertAllFav(changedFilmList: List<ChangedFilmItem>)

    @Update
    fun update(changedFilmItem: ChangedFilmItem)

    @Delete(entity = ChangedFilmItem::class)
    fun delete(changedFilmItem: ChangedFilmItem)

    @Query("DELETE FROM changed_film_table")
    fun deleteAllFavFilms()

    @Query("SELECT * FROM changed_film_table")
    fun getAllFav(): List<FilmItem>

}
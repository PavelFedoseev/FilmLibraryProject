package com.pavelprojects.filmlibraryproject.database.dao

import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem

@Dao
interface ChangedItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(changedFilmItem: ChangedFilmItem)

    @Insert
    fun insertAllFav(changedFilmList: List<ChangedFilmItem>)

    @Update
    fun update(changedFilmItem: ChangedFilmItem)

    @Delete(entity = ChangedFilmItem::class)
    fun delete(changedFilmItem: ChangedFilmItem)

    @Query("DELETE FROM changed_film_table WHERE isLiked!=1 AND isWatchLater!=1")
    fun deleteAllChangedFilms()

    @Query("SELECT * FROM changed_film_table")
    fun getAllChanged(): List<FilmItem>

    @Query("SELECT * FROM changed_film_table WHERE isLiked = 1")
    fun getAllFav(): List<FilmItem>

    @Query("SELECT * FROM changed_film_table WHERE isWatchLater = 1")
    fun getAllWatchLater(): List<ChangedFilmItem>

}
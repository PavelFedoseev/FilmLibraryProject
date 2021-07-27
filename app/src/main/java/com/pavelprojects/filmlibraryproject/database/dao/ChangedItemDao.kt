package com.pavelprojects.filmlibraryproject.database.dao

import androidx.room.*
import com.pavelprojects.filmlibraryproject.database.entity.ChangedFilmItem
import com.pavelprojects.filmlibraryproject.database.entity.FilmItem
import io.reactivex.Completable
import io.reactivex.Maybe

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
    fun getAllChanged(): Maybe<List<ChangedFilmItem>>

    @Query("SELECT * FROM changed_film_table WHERE isLiked = 1")
    fun getAllFav(): Maybe<List<FilmItem>>

    @Query("SELECT * FROM changed_film_table WHERE isWatchLater = 1")
    fun getAllWatchLater(): Maybe<List<ChangedFilmItem>>

    @Query("SELECT * FROM changed_film_table WHERE id = :id")
    fun getById(id: Int): Maybe<ChangedFilmItem>

}
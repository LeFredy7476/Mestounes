package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenre(genreEntity: GenreEntity)

    @Query("DELETE FROM genres WHERE trackPath = :trackPath")
    suspend fun deleteTrackGenres(trackPath: String)

    @Query("DELETE FROM genres WHERE trackPath = :trackPath AND name NOT IN (:genres)")
    suspend fun deleteOldTrackGenres(trackPath: String, genres: Set<String>)

    @Query("SELECT * FROM genres WHERE trackPath = :trackPath")
    suspend fun getTrackGenres(trackPath: String): List<GenreEntity>
}
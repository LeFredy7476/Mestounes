package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(genreEntity: GenreEntity)

    @Query("DELETE FROM genres WHERE trackPath = :trackPath")
    suspend fun deleteFromTrack(trackPath: String)

    @Query("DELETE FROM genres WHERE trackPath = :trackPath AND name NOT IN (:genres)")
    suspend fun deleteUnusedFromTrack(trackPath: String, genres: Set<String>)

    @Query("SELECT * FROM genres WHERE trackPath = :trackPath")
    suspend fun getFromTrack(trackPath: String): List<GenreEntity>
    @Query("SELECT * FROM genres WHERE trackPath = :trackPath")
    fun getFromTrackAsync(trackPath: String): Flow<List<GenreEntity>>

    @Query("SELECT name, COUNT(*) AS trackCount FROM genres GROUP BY name")
    suspend fun getAll(): Map<@MapColumn(columnName = "name") String, @MapColumn(columnName = "trackCount") Int>
    @Query("SELECT name, COUNT(*) AS trackCount FROM genres GROUP BY name")
    fun getAllAsync(): Flow<Map<@MapColumn(columnName = "name") String, @MapColumn(columnName = "trackCount") Int>>
}
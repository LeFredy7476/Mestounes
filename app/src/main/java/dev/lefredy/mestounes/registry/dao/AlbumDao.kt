package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Transaction @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks WHERE name = :name AND albumArtist = :albumArtist GROUP BY album, albumArtist ORDER BY name ASC")
    suspend fun get(name: String, albumArtist: String): AlbumEntity
    @Transaction @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks WHERE name = :name AND albumArtist = :albumArtist GROUP BY album, albumArtist ORDER BY name ASC")
    fun getAsync(name: String, albumArtist: String): Flow<AlbumEntity>

    @Transaction @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks GROUP BY album, albumArtist ORDER BY name ASC")
    suspend fun getAll(): List<AlbumEntity>
    @Transaction @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks GROUP BY album, albumArtist ORDER BY name ASC")
    fun getAllAsync(): Flow<List<AlbumEntity>>

    @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks WHERE albumArtist = :albumArtist GROUP BY album, albumArtist ORDER BY date ASC")
    suspend fun getByAlbumArtist(albumArtist: String): List<AlbumEntity>
    @Query("SELECT album AS name, albumArtist, COUNT(*) AS trackCount, MAX(date) AS date, SUM(mediaLength) AS totalLength FROM tracks WHERE albumArtist = :albumArtist GROUP BY album, albumArtist ORDER BY date ASC")
    fun getByAlbumArtistAsync(albumArtist: String): Flow<List<AlbumEntity>>

    @Query("SELECT albumArtist FROM tracks GROUP BY albumArtist")
    suspend fun getArtists(): Set<String>
    @Query("SELECT albumArtist FROM tracks GROUP BY albumArtist")
    fun getArtistsAsync(): Flow<Set<String>>
}
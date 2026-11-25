package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Upsert
    suspend fun upsert(trackEntity: TrackEntity)

    @Delete
    suspend fun delete(trackEntity: TrackEntity)

    @Transaction @Query("SELECT * FROM tracks")
    suspend fun getAll(): List<TrackWithArtist>
    @Transaction @Query("SELECT * FROM tracks")
    fun getAllAsync(): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT * FROM tracks WHERE path = :path")
    suspend fun get(path: String): TrackWithArtist?
    @Transaction @Query("SELECT * FROM tracks WHERE path = :path")
    fun getAsync(path: String): Flow<TrackWithArtist?>

    @Transaction @Query("SELECT * FROM tracks WHERE path = :path")
    suspend fun getRaw(path: String): TrackEntity?
    @Transaction @Query("SELECT * FROM tracks WHERE path = :path")
    fun getRawAsync(path: String): Flow<TrackEntity?>

    @Transaction @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist")
    suspend fun getByAlbum(album: String, albumArtist: String): List<TrackWithArtist>
    @Transaction @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist")
    fun getByAlbumAsync(album: String, albumArtist: String): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist ORDER BY trackNumber, discNumber")
    suspend fun getByAlbumOrdered(album: String, albumArtist: String): List<TrackWithArtist>
    @Transaction @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist ORDER BY trackNumber, discNumber")
    fun getByAlbumOrderedAsync(album: String, albumArtist: String): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT * FROM tracks WHERE albumArtist = :albumArtist")
    suspend fun getByAlbumArtist(albumArtist: String): List<TrackWithArtist>
    @Transaction @Query("SELECT * FROM tracks WHERE albumArtist = :albumArtist")
    fun getByAlbumArtistAsync(albumArtist: String): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT * FROM tracks WHERE RTRIM(path, REPLACE(path, '/', '')) = :directory ORDER BY path")
    suspend fun getByDirectory(directory: String): List<TrackWithArtist>
    @Transaction @Query("SELECT * FROM tracks WHERE RTRIM(path, REPLACE(path, '/', '')) = :directory ORDER BY path")
    fun getByDirectoryAsync(directory: String): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT t.* FROM tracks t WHERE EXISTS (SELECT 1 FROM genres g WHERE g.trackPath = t.path and g.name = :genre) ORDER BY t.title")
    suspend fun getByGenre(genre: String): List<TrackWithArtist>
    @Transaction @Query("SELECT t.* FROM tracks t WHERE EXISTS (SELECT 1 FROM genres g WHERE g.trackPath = t.path and g.name = :genre) ORDER BY t.title")
    fun getByGenreAsync(genre: String): Flow<List<TrackWithArtist>>

    @Transaction @Query("SELECT t.* FROM tracks t WHERE EXISTS (SELECT 1 FROM artists a WHERE a.trackPath = t.path and a.name = :artist) ORDER BY t.album, t.discNumber, t.trackNumber")
    suspend fun getByArtist(artist: String): List<TrackWithArtist>
    @Transaction @Query("SELECT t.* FROM tracks t WHERE EXISTS (SELECT 1 FROM artists a WHERE a.trackPath = t.path and a.name = :artist) ORDER BY t.album, t.discNumber, t.trackNumber")
    fun getByArtistAsync(artist: String): Flow<List<TrackWithArtist>>
}
package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface TrackDao {
    @Upsert
    suspend fun insertTrack(trackEntity: TrackEntity)

    @Delete
    suspend fun deleteTrack(trackEntity: TrackEntity)

    @Transaction
    @Query("SELECT * FROM tracks")
    suspend fun getTracks(): List<TrackWithArtist>

    @Transaction
    @Query("SELECT * FROM tracks WHERE path = :path")
    suspend fun getTrack(path: String): TrackWithArtist

    @Transaction
    @Query("SELECT * FROM tracks WHERE path = :path")
    suspend fun getTrackWithGenre(path: String): TrackWithArtistAndGenre

    @Transaction
    @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist")
    suspend fun getTrackByAlbum(album: String, albumArtist: String): List<TrackWithArtist>

    @Transaction
    @Query("SELECT * FROM tracks WHERE album = :album AND albumArtist = :albumArtist ORDER BY trackNumber, discNumber")
    suspend fun getTrackByAlbumOrdered(album: String, albumArtist: String): List<TrackWithArtist>

    @Transaction
    @Query("SELECT * FROM tracks WHERE albumArtist = :albumArtist")
    suspend fun getTrackByAlbumArtist(albumArtist: String): List<TrackWithArtist>
}
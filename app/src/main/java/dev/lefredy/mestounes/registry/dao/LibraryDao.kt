package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface LibraryDao {
    @Insert
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("SELECT * FROM Track")
    suspend fun getAllTrack(): List<Track>

    @Query("SELECT * FROM Track WHERE trackId = :trackId")
    suspend fun getOneTrack(trackId: Long): Track

    @Query("SELECT * FROM Track WHERE album = :album AND albumartist = :albumartistId")
    suspend fun getTrackByAlbum(album: String, albumartistId: Long): List<Track>

    @Query("SELECT * FROM Track WHERE albumartist = :albumartistId")
    suspend fun getTrackByAlbumArtist(albumartistId: Long): List<Track>

    @Query("SELECT * FROM Track WHERE path = :path")
    suspend fun getTrackByPath(path: String): Track

    @Transaction
    @Query("SELECT * FROM Track WHERE trackId = :trackId")
    suspend fun getOneTrackWithArtist(trackId: Long): TrackWithArtist

    @Transaction
    @Query("SELECT * FROM Track WHERE path = :path")
    suspend fun getTrackByPathWithArtist(path: String): TrackWithArtist
}
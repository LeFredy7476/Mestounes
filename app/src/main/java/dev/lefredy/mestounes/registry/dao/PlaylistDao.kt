package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.PlaylistEntity
import dev.lefredy.mestounes.registry.entity.PlaylistItem
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist

@Dao
interface PlaylistDao {
    // TODO: add async alts to all of those

    @Transaction @Query("SELECT * FROM tracks WHERE path = :path")
    suspend fun getTrack(path: String): TrackWithArtist? // I KNOW IT'S A DUPLICATE, for the sake of simplicity lemme keep that one

    @Query("SELECT * FROM playlists WHERE deleted = 0 ORDER BY owned DESC, name ASC")
    suspend fun getAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists ORDER BY owned DESC, name ASC")
    suspend fun getAllAndDeleted(): List<PlaylistEntity>

    @Transaction @Query("SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON t.path = pt.path WHERE pt.id = :playlist ORDER BY pt.`index` ASC")
    suspend fun getTracksFromPlaylist(playlist: Long): List<TrackEntity>

    @Query("SELECT * FROM playlist_tracks WHERE id = :playlistId ORDER BY `index`")
    suspend fun getPlaylistItems(playlistId: Long): List<PlaylistItem>

    @Transaction
    suspend fun getPlaylistTracks(playlistId: Long): List<TrackWithArtist> = getPlaylistItems(playlistId).mapNotNull { getTrack(it.path) }

    @Query("DELETE FROM playlist_tracks WHERE id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fillPlaylist(playlistItems: List<PlaylistItem>)

    @Insert
    suspend fun insertPlaylist(playlistEntity: PlaylistEntity): PlaylistEntity

    @Delete
    suspend fun deletePlaylist(playlistEntity: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlistEntity: PlaylistEntity)
}
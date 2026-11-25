package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.PlaylistEntity
import dev.lefredy.mestounes.utils.currentTimestamp
import java.util.UUID

class Playlist private constructor(
    var id: Long?,
    var name: String,
    val owned: Boolean,
    var playlistRef: UUID?, // null if local only
    var updatedAt: Long,
    var deleted: Boolean,
    val tracks: MutableList<Track>
) {
    constructor (playlistEntity: PlaylistEntity): this (
        playlistEntity.id,
        playlistEntity.name,
        playlistEntity.owned,
        playlistEntity.playlistRef,
        playlistEntity.updatedAt,
        playlistEntity.deleted,
        mutableListOf()
    )

    fun toPlaylistEntity() = PlaylistEntity(
        id, name, owned, playlistRef, updatedAt, deleted
    )

    suspend fun fetchTracks(): Playlist {
        if (tracks.isEmpty() && id != null) {
            tracks.addAll(LocalLibrary.require().getPlaylistTracks(this))
        }
        return this
    }
    suspend fun forceFetchTracks(): Playlist {
        tracks.clear()
        tracks.addAll(LocalLibrary.require().getPlaylistTracks(this))
        return this
    }

    suspend fun save(): Playlist {
        val localLibrary = LocalLibrary.require()
        updatedAt = currentTimestamp()
        if (id == null) {
            id = localLibrary.insertPlaylist(this).id
        }
        else {
            localLibrary.updatePlaylist(this)
        }
        if (owned && playlistRef != null) {
            // call server for update
        }
        return this
    }

    suspend fun saveWithTracks(): Playlist {
        val localLibrary = LocalLibrary.require()
        updatedAt = currentTimestamp()
        if (id == null) {
            id = localLibrary.insertPlaylist(this).id
        }
        else {
            localLibrary.updatePlaylist(this)
        }
        localLibrary.fillPlaylist(this)
        if (owned && playlistRef != null) {
            // call server for update
        }
        return this
    }

    suspend fun delete() {
        if (id == null) return
        deleted = true
        tracks.clear()
        updatedAt = currentTimestamp()
        saveWithTracks()
        if (owned && playlistRef != null) {
            // call server for deletion
            // if succesful, call LocalLibrary.require().deletePlaylist(this)
        }
    }
}
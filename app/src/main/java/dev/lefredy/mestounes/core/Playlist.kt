package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.PlaylistEntity
import java.util.UUID

class Playlist private constructor(
    var id: Long?,
    var name: String,
    val owned: Boolean,
    var playlistRef: UUID?, // null if local only
    var updatedAt: Long,
    var deleted: Boolean,
    var tracks: MutableList<Track>?
) {
    constructor (playlistEntity: PlaylistEntity): this (
        playlistEntity.id,
        playlistEntity.name,
        playlistEntity.owned,
        playlistEntity.playlistRef,
        playlistEntity.updatedAt,
        playlistEntity.deleted,
        null
    )

    companion object {
        suspend fun createPlaylist(name: String, tracks: List<Track>): Playlist {
            return Playlist(null, name, true, null, System.currentTimeMillis(), false, tracks.toMutableList()).saveWithTracks()
        }
    }

    fun toPlaylistEntity() = PlaylistEntity(
        id, name, owned, playlistRef, updatedAt, deleted
    )

    suspend fun fetchTracks(): Playlist {
        if (tracks == null && id != null) {
            tracks = LocalLibrary.require().getPlaylistTracks(this).toMutableList()
        }
        return this
    }
    suspend fun forceFetchTracks(): Playlist {
        tracks = LocalLibrary.require().getPlaylistTracks(this).toMutableList()
        return this
    }

    suspend fun save(): Playlist {
        val localLibrary = LocalLibrary.require()
        updatedAt = System.currentTimeMillis()
        if (id == null) {
            id = localLibrary.insertPlaylist(this)
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
        updatedAt = System.currentTimeMillis()
        if (id == null) {
            id = localLibrary.insertPlaylist(this)
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
        if (playlistRef == null) {
            return LocalLibrary.require().deletePlaylist(this)
        }
        deleted = true
        tracks?.clear()
        updatedAt = System.currentTimeMillis()
        saveWithTracks()
        if (owned && playlistRef != null) {
            // call server for deletion
            // if succesful, call LocalLibrary.require().deletePlaylist(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playlist) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }
}
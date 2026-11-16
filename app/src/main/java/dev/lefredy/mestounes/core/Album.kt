package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary
import kotlinx.coroutines.flow.Flow

class Album (
    val name: String,
    val albumArtist: Artist,
    val trackCount: Int? = null,
    val date: Int? = null,
    val totalLength: Long? = null,
) {
    constructor(albumEntity: dev.lefredy.mestounes.registry.entity.AlbumEntity): this(
        albumEntity.name,
        Artist(albumEntity.albumArtist),
        albumEntity.trackCount,
        albumEntity.date,
        albumEntity.totalLength,
    )

    suspend fun fetchInfo(): Album {
        if (trackCount == null) {
            LocalLibrary.require().getAlbum(name, albumArtist)
        }
        return this
    }

    suspend fun getTracks(): List<Track> {
        return LocalLibrary.require().getTracksByAlbum(this)
    }
    suspend fun getTracksAsync(): Flow<List<Track>> {
        return LocalLibrary.require().getTracksByAlbumAsync(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Album) return false
        return name == other.name && albumArtist == other.albumArtist
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + albumArtist.hashCode()
    }
}
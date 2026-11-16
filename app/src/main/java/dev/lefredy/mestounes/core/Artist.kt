package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary

class Artist (
    val name: String
) {
    constructor (artistItem: dev.lefredy.mestounes.registry.entity.ArtistItem): this (
        artistItem.name
    )

    fun toArtistEntity(trackPath: String, index: Number) = dev.lefredy.mestounes.registry.entity.ArtistEntity(
        trackPath, name, index.toByte()
    )

    suspend fun getTracks() =
        LocalLibrary.require().getTracksByArtist(this)
    fun getTracksAsync() =
        LocalLibrary.get().getTracksByArtistAsync(this)

    suspend fun getAlbums() =
        LocalLibrary.require().getAlbumsByAlbumArtist(this)
    fun getAlbumsAsync() =
        LocalLibrary.get().getAlbumsByAlbumArtistAsync(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
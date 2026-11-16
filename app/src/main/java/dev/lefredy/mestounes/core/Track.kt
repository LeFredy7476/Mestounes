package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist

class Track private constructor (
    val path: String, // PRIMARY KEY, relative path to the root folder
    var trackId: Long? = null, // LEGACY, server id (null means local only)
    var title: String, // if missing, default to filename
    val mediaLength: Long, // in millisecond
    var artists: MutableList<Artist>, // if empty, shows "unknown" in UI
    var album: String, // if missing, default to parent directory name
    var albumArtist: Artist, // if missing, default to first item in artist list if available.
    var composer: String? = null, // if missing, default to albumArtist
    var date: Int? = null, // if null, shows "unknown" in UI
    var genres: MutableSet<Genre>? = null, // if empty, shows "unknown" in UI (null = not fetched yet)
    var trackNumber: Int = 0, // if missing, default to 0
    var discNumber: Int = 0, // if missing, default to 0
    var comment: String = "", // can be empty. PS: comments are static to the original file and not user-defined
) {
    constructor (trackEntity: TrackWithArtist): this (
        trackEntity.track.path,
        trackEntity.track.trackId,
        trackEntity.track.title,
        trackEntity.track.mediaLength,
        trackEntity.artists.map(::Artist).toMutableList(),
        trackEntity.track.album,
        Artist(trackEntity.track.albumArtist),
        trackEntity.track.composer,
        trackEntity.track.date,
        null,
        trackEntity.track.trackNumber,
        trackEntity.track.discNumber,
        trackEntity.track.comment,
    )

    fun toTrackEntity() = TrackEntity(
        path, trackId, title, mediaLength, album, albumArtist.name,
        composer, date, trackNumber, discNumber, comment,
    )

    suspend fun fetchGenres(): Track {
        genres = genres ?: LocalLibrary.require().getTrackGenres(this).toMutableSet()
        return this
    }

    suspend fun save() {
        LocalLibrary.require().saveTrack(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false
        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
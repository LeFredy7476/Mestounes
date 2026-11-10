package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    primaryKeys = ["trackId", "artistId"],
    foreignKeys = [
        ForeignKey(Track::class, ["trackId"], ["trackId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Artist::class, ["artistId"], ["artistId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TrackArtistCrossRef(
    val trackId: Long,
    val artistId: Long
)

@Entity(
    primaryKeys = ["trackId", "genreId"],
    foreignKeys = [
        ForeignKey(Track::class, ["trackId"], ["trackId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Genre::class, ["genreId"], ["genreId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TrackGenreCrossRef(
    val trackId: Long,
    val genreId: Long
)

@Entity(
    primaryKeys = ["trackId", "playlistId"],
    foreignKeys = [
        ForeignKey(Track::class, ["trackId"], ["trackId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Playlist::class, ["playlistId"], ["playlistId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TrackPlaylistCrossRef(
    val trackId: Long,
    val playlistId: Long
)

data class TrackWithArtist(
    @Embedded val track: Track,
    @Relation(
        Artist::class,
        ["trackId"],
        ["artistId"],
        Junction(TrackArtistCrossRef::class)
    ) val artists: List<Artist>
)

data class TrackWithArtistGenre(
    @Embedded val track: Track,
    @Relation(
        Artist::class,
        ["trackId"],
        ["artistId"],
        Junction(TrackArtistCrossRef::class)
    ) val artists: List<Artist>,
    @Relation(
        Genre::class,
        ["trackId"],
        ["genreId"],
        Junction(TrackGenreCrossRef::class)
    ) val genres: List<Artist>
)
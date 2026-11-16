package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

data class GenreName(
    val name: String,
)

data class ArtistItem(
    val name: String,
    val index: Byte,
)

data class TrackWithGenre ( // never used technically
    @Embedded
    val track: TrackEntity,
    @Relation(GenreEntity::class, ["path"], ["trackPath"])
    val genres: List<GenreName>,
)

data class TrackWithArtist (
    @Embedded
    val track: TrackEntity,
    @Relation(ArtistEntity::class, ["path"], ["trackPath"])
    val artists: List<ArtistItem>,
)

data class TrackWithArtistAndGenre (
    @Embedded
    val track: TrackEntity,
    @Relation(ArtistEntity::class, ["path"], ["trackPath"])
    val artists: List<ArtistItem>,
    @Relation(GenreEntity::class, ["path"], ["trackPath"])
    val genres: List<GenreName>,
)
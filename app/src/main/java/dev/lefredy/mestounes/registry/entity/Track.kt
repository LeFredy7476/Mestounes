package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
        ForeignKey(Artist::class, ["artistId"], ["albumArtist"]),
        ForeignKey(Album::class, ["albumId"], ["album"]),
    ]
)
data class Track (
    @PrimaryKey val trackId: Long,
    @ColumnInfo(index = true) val path: String,
    val title: String,
    val album: Long,
    val albumArtist: Long?,
    val year: Long,
    val uri: String,
    val trackNb: Int,
    val discNb: Int
)

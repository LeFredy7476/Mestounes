package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
        ForeignKey(Artist::class, ["artistId"], ["artist"]),
        ForeignKey(Track::class, ["trackId"], ["coverTrack"]),
    ]
)
data class Album (
    @PrimaryKey val albumId: Long,
    val name: String,
    val year: Int,
    val artist: Long?,
    val coverTrack: Long?,
    val trackTotal: Int,
    val discTotal: Int
)
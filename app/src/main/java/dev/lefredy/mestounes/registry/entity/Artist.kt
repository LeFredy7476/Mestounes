package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
        ForeignKey(Artist::class, ["artistId"], ["alias"]),
        ForeignKey(Track::class, ["trackId"], ["coverTrack"]),
    ]
)
data class Artist (
    @PrimaryKey val artistId: Long,
    val name: String,
    val alias: Long?,
    val coverTrack: Long?
)
package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
//        ForeignKey(User::class, ["userId"], ["creator"]),
        ForeignKey(Track::class, ["trackId"], ["coverTrack"])
    ]
)
data class Playlist (
    @PrimaryKey val playlistId: Long,
    val name: String,
//    val creator: Long?,
    val coverTrack: Long?
)
package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity
data class PlaylistEntity (
    @PrimaryKey val playlistId: Long,
    val name: String,
//    val creator: Long?,
)
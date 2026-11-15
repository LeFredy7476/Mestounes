package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    tableName = "tracks"
)
data class TrackEntity (
    @PrimaryKey val path: String,
    val trackId: Long?, // server id, (null means local only)
    val title: String,
    val album: String,
    val albumArtist: String?,
    val composer: String?,
    val date: Int?,
    val trackNumber: Int,
    val discNumber: Int,
    val comment: String,
)

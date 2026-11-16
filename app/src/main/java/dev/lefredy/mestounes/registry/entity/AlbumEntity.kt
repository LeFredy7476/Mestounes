package dev.lefredy.mestounes.registry.entity

import androidx.room3.ColumnInfo

data class AlbumEntity (
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "albumArtist") val albumArtist: String,
    @ColumnInfo(name = "trackCount") val trackCount: Int,
    @ColumnInfo(name = "date") val date: Int?,
    @ColumnInfo(name = "totalLength") val totalLength: Long?,
)
package dev.lefredy.mestounes.registry.entity

import androidx.room3.ColumnInfo

data class Album(
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "albumArtist") val albumArtist: String,
    @ColumnInfo(name = "trackCount") val trackCount: Int,
    @ColumnInfo(name = "date") val date: Int,
)
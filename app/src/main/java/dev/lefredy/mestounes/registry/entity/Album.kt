package dev.lefredy.mestounes.registry.entity

import androidx.room3.ColumnInfo

data class Album(
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "albumartist") val albumartist: String,
    @ColumnInfo(name = "trackcount") val trackcount: Int,
    @ColumnInfo(name = "date") val date: Int,
)
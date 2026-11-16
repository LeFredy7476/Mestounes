package dev.lefredy.mestounes.registry.entity

import androidx.room3.ColumnInfo

data class DirectoryEntity(
    @ColumnInfo(name = "directory") val directory: String,
    @ColumnInfo(name = "trackCount") val trackCount: Int,
)
package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    tableName = "genres",
    primaryKeys = ["trackPath", "name"],
    foreignKeys = [
        ForeignKey(TrackEntity::class, ["path"], ["trackPath"], onDelete = ForeignKey.CASCADE)
    ]
)
data class GenreEntity (
    val trackPath: String,
    val name: String,
    // unordered
)
package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    tableName = "artists",
    primaryKeys = ["trackPath", "name"],
    foreignKeys = [
        ForeignKey(TrackEntity::class, ["path"], ["trackPath"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ArtistEntity (
    val trackPath: String,
    val name: String,
    val index: Byte, // ordered
)
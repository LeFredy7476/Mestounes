package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity
data class Genre (
    @PrimaryKey val genreId: Long,
    val name: String,
    val id3v1: Int
)
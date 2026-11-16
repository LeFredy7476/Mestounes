package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary

class Genre (
    val name: String,
    val trackCount: Int? = null,
) {
    constructor (genre: Map.Entry<String, Int>): this (
        genre.key,
        genre.value,
    )

    constructor (genreEntity: dev.lefredy.mestounes.registry.entity.GenreEntity): this (
        genreEntity.name,
        null,
    )

    suspend fun getTracks() =
        LocalLibrary.require().getTracksByGenre(this)
    fun getTracksAsync() =
        LocalLibrary.get().getTracksByGenreAsync(this)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Genre) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
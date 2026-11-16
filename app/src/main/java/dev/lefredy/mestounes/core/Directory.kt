package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LocalLibrary

class Directory (
    val directory: String,
    val trackCount: Int,
) {
    constructor (directoryEntity: dev.lefredy.mestounes.registry.entity.DirectoryEntity): this (
        directoryEntity.directory,
        directoryEntity.trackCount,
    )

    suspend fun getTracks() =
        LocalLibrary.require().getTracksByDirectory(this)
    fun getTracksAsync() =
        LocalLibrary.get().getTracksByDirectoryAsync(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Directory) return false
        return directory == other.directory
    }

    override fun hashCode(): Int {
        return directory.hashCode()
    }
}
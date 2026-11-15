package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface AlbumDao {
    @Transaction
    @Query("SELECT album, albumArtist, COUNT(*) AS trackCount, MAX(date) as date FROM tracks GROUP BY album, albumartist")
    suspend fun getAlbums(): AlbumEntity
}
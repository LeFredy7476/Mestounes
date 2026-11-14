package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface AlbumDao {
    @Transaction
    @Query("SELECT album, albumartist, COUNT(*) AS trackcount, MAX(date) as date FROM Track GROUP BY album, albumartist")
    suspend fun getAlbums(): Album
}
package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*

@Dao
interface ArtistDao {
    @Upsert
    suspend fun insertArtist(artistEntity: ArtistEntity)

    @Query("DELETE FROM artists WHERE trackPath = :trackPath")
    suspend fun deleteTrackArtists(trackPath: String)

    @Query("SELECT * FROM artists WHERE trackPath = :trackPath ORDER BY `index`")
    suspend fun getTrackArtists(trackPath: String): List<ArtistEntity>
}
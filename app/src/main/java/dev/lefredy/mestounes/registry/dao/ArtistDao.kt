package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Upsert
    suspend fun upsert(artistEntity: ArtistEntity)

    @Query("DELETE FROM artists WHERE trackPath = :trackPath")
    suspend fun deleteFromTrack(trackPath: String)

    @Query("SELECT * FROM artists WHERE trackPath = :trackPath ORDER BY `index`")
    suspend fun getFromTrack(trackPath: String): List<ArtistEntity>
    @Query("SELECT * FROM artists WHERE trackPath = :trackPath ORDER BY `index`")
    fun getFromTrackAsync(trackPath: String): Flow<List<ArtistEntity>>

    @Query("SELECT name FROM artists GROUP BY name")
    suspend fun getAll(): Set<String>
    @Query("SELECT name FROM artists GROUP BY name")
    fun getAllAsync(): Flow<Set<String>>

}
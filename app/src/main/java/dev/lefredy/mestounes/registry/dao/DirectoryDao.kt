package dev.lefredy.mestounes.registry.dao

import androidx.room3.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectoryDao {
    @Query("SELECT RTRIM(path, REPLACE(path, '/', '')) AS directory, COUNT(*) AS trackCount FROM tracks GROUP BY directory ORDER BY directory ASC")
    suspend fun getAll(): List<DirectoryEntity>
    @Query("SELECT RTRIM(path, REPLACE(path, '/', '')) AS directory, COUNT(*) AS trackCount FROM tracks GROUP BY directory ORDER BY directory ASC")
    fun getAllAsync(): Flow<List<DirectoryEntity>>
}
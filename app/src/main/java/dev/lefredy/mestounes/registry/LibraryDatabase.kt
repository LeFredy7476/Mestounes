package dev.lefredy.mestounes.registry

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.lefredy.mestounes.registry.dao.AlbumDao
import dev.lefredy.mestounes.registry.dao.ArtistDao
import dev.lefredy.mestounes.registry.dao.DirectoryDao
import dev.lefredy.mestounes.registry.dao.GenreDao
import dev.lefredy.mestounes.registry.dao.TrackDao
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.CompletableDeferred

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Database(
    version = 1,
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
//        PlaylistEntity::class,
    ]
)
abstract class LibraryDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun genreDao(): GenreDao
    abstract fun artistDao(): ArtistDao
    abstract fun directoryDao(): DirectoryDao

    companion object {
        private val instanceDeferred = CompletableDeferred<LibraryDatabase>()
        fun require(context: android.content.Context): LibraryDatabase {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library.db"
                ).build()
                instanceDeferred.complete(db)
                return db
            }
        }
        suspend fun require(): LibraryDatabase {
            return instanceDeferred.await()
        }
    }

}
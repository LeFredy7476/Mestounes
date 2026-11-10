package dev.lefredy.mestounes.registry

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.lefredy.mestounes.registry.dao.LibrairyDao
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.CompletableDeferred

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Database(
    version = 1,
    entities = [
        Track::class,
        Artist::class,
        Album::class,
        Genre::class,
        Playlist::class,
        TrackArtistCrossRef::class,
        TrackGenreCrossRef::class,
        TrackPlaylistCrossRef::class
    ]
)
abstract class LibrairyDatabase: RoomDatabase() {
    abstract fun librairyDao(): LibrairyDao

    companion object {

        private val instanceDeferred = CompletableDeferred<LibrairyDatabase>()

        fun require(context: android.content.Context): LibrairyDatabase {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    LibrairyDatabase::class.java,
                    "librairy.db"
                ).build()
                instanceDeferred.complete(db)
                return db
            }
        }

        suspend fun require(): LibrairyDatabase {
            return instanceDeferred.await()
        }
    }
}
package dev.lefredy.mestounes.registry

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.lefredy.mestounes.registry.dao.LibraryDao
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.CompletableDeferred

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Database(
    version = 1,
    entities = [
//        Source::class,
        Track::class,
        Artist::class,
//        Album::class,
        Genre::class,
        Playlist::class,
        TrackArtistCrossRef::class,
        TrackGenreCrossRef::class,
        TrackPlaylistCrossRef::class
    ]
)
abstract class LibraryDatabase: RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {

        private val instanceDeferred = CompletableDeferred<LibraryDatabase>()

        fun require(context: android.content.Context): LibraryDatabase {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "librairy.db"
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
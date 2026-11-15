package dev.lefredy.mestounes.registry

import androidx.room3.Room
import dev.lefredy.mestounes.core.Track
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LocalRepository(
    private val libraryDatabase: LibraryDatabase
) {
    companion object {
        private val instanceDeferred = CompletableDeferred<LocalRepository>()


        fun require(context: android.content.Context): LocalRepository {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val lr = LocalRepository(Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library.db"
                ).build())
                instanceDeferred.complete(lr)
                return lr
            }
        }
        suspend fun require(): LocalRepository {
            return instanceDeferred.await()
        }
    }

    suspend fun getTracks() =
        libraryDatabase
            .trackDao()
            .getTracks()
            .map(Track::importRoomEntity)

    suspend fun getTrackByAlbum(album: String, albumArtist: String) =
        libraryDatabase
            .trackDao()
            .getTrackByAlbumOrdered(album, albumArtist)
            .map(Track::importRoomEntity)
}
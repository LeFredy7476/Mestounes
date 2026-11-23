package dev.lefredy.mestounes.modules

import android.net.Uri
import androidx.media3.common.PlaybackException
import dev.lefredy.mestounes.core.Track
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class FileProvider private constructor(
    val context: android.content.Context
) {
    companion object {
        private val instanceDeferred = CompletableDeferred<FileProvider>()
        fun require(context: android.content.Context): FileProvider {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val fileProvider = FileProvider(context.applicationContext)
                instanceDeferred.complete(fileProvider)
                return fileProvider
            }
        }
        suspend fun require(): FileProvider {
            return instanceDeferred.await()
        }
        fun get(): FileProvider {
            return instanceDeferred.getCompleted()
        }
    }

    //temporary
    val lastUpdate: Long
        get() = 0

    fun onStartup(): Unit {
        // TODO: start a thread to scan library
        // get last update
        // loop through library and get all tracks with no trackId
        //   - for each, check if file still exist, if not : delete the entry
        // scan storage for any item which las modified date is later than last update
        //   - overwrite database entry
    }


    fun getTrackURI(track: Track): Uri? {
        // TODO: return URI of requested file
        // check if the file exist in storage
        // if not:
        //   - if trackId is present: return stream URI
        //   - if no trackId: remove entry from db, return null
        // if yes:
        //   - return local uri

        return null
    }

    fun onPlaybackError(track: Track, error: PlaybackException): Unit {
        // TODO: check the cause of the error, act accordingly
    }
}
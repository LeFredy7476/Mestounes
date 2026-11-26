package dev.lefredy.mestounes.modules

import android.net.Uri
import androidx.media3.common.PlaybackException
import dev.lefredy.mestounes.core.Track
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.storage.Stockage

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
    var lastUpdate: Long
        get() = try { Stockage(context).chargerLong("lastUpdate") } catch (e: Exception) { 0L }
        set(value) = try { Stockage(context).sauvegarderLong("lastUpdate", value) } catch (e: Exception) {}

    fun onStartup(): Unit {
        // TODO: start a thread to scan library
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val library = LocalLibrary.require(context)
                // loop through library and get all tracks with no trackId
                //   - for each, check if file still exist, if not : delete the entry
                val currentTracks = library.getTracks()
                for (track in currentTracks) {
                    if (track.trackId == null) {
                        val file = File(track.path)
                        val resolvedFile = if (file.isAbsolute) file else File("/storage/emulated/0/Music", track.path)
                        if (!resolvedFile.exists()) {
                            library.deleteTrack(track)
                            Log.d("FileProvider", "Removed deleted local track: ${track.path}")
                        }
                    }
                }
                // get last update
                // scan storage for any item which las modified date is later than last update
                //   - overwrite database entry
                val musicDirectory = File("/storage/emulated/0/Music")
                if (musicDirectory.exists() && musicDirectory.isDirectory) {
                    val mp3Files = musicDirectory.walkTopDown().filter {
                        it.isFile && it.extension.equals("mp3", ignoreCase = true)
                    }
                    for (file in mp3Files) {
                        if (file.lastModified() > lastUpdate) {
                            try {
                                val track = Track.fromMetadata(context, Uri.fromFile(file))
                                if (library.getTrackRaw(track.path)?.trackId == null) {
                                    // only upsert file in library if it does not conflict with a server-owned entry
                                    library.saveTrack(track)
                                    Log.d("FileProvider", "Indexed local track: ${track.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("FileProvider", "Failed to parse: ${file.absolutePath}", e)
                            }
                        }
                    }
                    lastUpdate = System.currentTimeMillis()
                } else {
                    Log.d("FileProvider", "Library root folder doesn't exist")
                }
            } catch (e: Exception) {
                Log.e("FileProvider", "Error during onStartup scan", e)
            }
        }
    }

    fun getTrackURI(track: Track): Uri? {
        // TODO: return URI of requested file
        val file = File(track.path)
        val resolvedFile = if (file.isAbsolute) file else File("/storage/emulated/0/Music", track.path)
        // check if the file exist in storage
        // if yes:
        //   - return local uri
        if (resolvedFile.exists()) {
            return Uri.fromFile(resolvedFile)
        }
        // if not:
        //   - if trackId is present: return stream URI
        //   - if no trackId: remove entry from db, return null
        return if (track.trackId != null) {
            Uri.parse("https://api.lestounes/v1/library/track/file/${track.trackId}")
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                LocalLibrary.require(context).deleteTrack(track)
            }
            null
        }
    }

    fun onPlaybackError(track: Track, error: PlaybackException): Unit {
        // TODO: check the cause of the error, act accordingly
        // Log the error
        Log.e("FileProvider", "Playback error [${error.errorCodeName}] on track: ${track.title} (${track.path})", error)

        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                if (track.trackId == null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        LocalLibrary.require(context).deleteTrack(track)
                    }
                }
            }
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                Log.w("FileProvider", "Network lost while streaming ${track.title}")
            }
            PlaybackException.ERROR_CODE_DECODING_FAILED, PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> {
                Log.w("FileProvider", "Corrupted or unreadable audio file for ${track.title}")
            }
            else -> {
                Log.w("FileProvider", "Unhandled playback error code: ${error.errorCode}")
            }
        }
    }
}
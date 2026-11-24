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
import dev.lefredy.mestounes.registry.LibraryDatabase
import android.util.Log
import android.media.MediaMetadataRetriever
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import dev.lefredy.mestounes.registry.entity.ArtistItem

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = LibraryDatabase.require(context)
                val library = LocalLibrary.require(context)
                // loop through library and get all tracks with no trackId
                //   - for each, check if file still exist, if not : delete the entry
                val currentTracks = library.getTracks()
                for (track in currentTracks) {
                    if (track.trackId == null) {
                        val file = File(track.path)
                        val resolvedFile = if (file.isAbsolute) file else File("/storage/emulated/0/Music", track.path)
                        if (!resolvedFile.exists()) {
                            db.trackDao().delete(track.toTrackEntity())
                            db.artistDao().deleteFromTrack(track.path)
                            db.genreDao().deleteUnusedFromTrack(track.path, emptySet())
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
                                val track = parseTrackFromFile(file)
                                if (track != null) {
                                    library.saveTrack(track)
                                    Log.d("FileProvider", "Indexed local track: ${track.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("FileProvider", "Failed to parse: ${file.absolutePath}", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FileProvider", "Error during onStartup scan", e)
            }
        }
    }

    // helper function that extracts the metadata from a local file and maps it into a Track that's ready for database insertion
    private fun parseTrackFromFile(file: File): Track? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.nameWithoutExtension
            val artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: file.parentFile?.name ?: "Unknown"
            val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                ?: artistName
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull()
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.split("/")?.firstOrNull()?.toIntOrNull() ?: 0
            val discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                ?.split("/")?.firstOrNull()?.toIntOrNull() ?: 0

            val trackEntity = TrackEntity(
                path = file.absolutePath,
                trackId = null,
                title = title,
                mediaLength = duration,
                album = album,
                albumArtist = albumArtist,
                composer = null,
                date = date,
                trackNumber = trackNumber,
                discNumber = discNumber,
                comment = ""
            )

            Track(TrackWithArtist(
                    track = trackEntity,
                    artists = listOf(ArtistItem(name = artistName, index = 0))
                )
            )
            
        } finally {
            retriever.release()
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
                val db = LibraryDatabase.require(context)
                db.trackDao().delete(track.toTrackEntity())
                db.artistDao().deleteFromTrack(track.path)
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
                        val db = LibraryDatabase.require(context)
                        db.trackDao().delete(track.toTrackEntity())
                        db.artistDao().deleteFromTrack(track.path)
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
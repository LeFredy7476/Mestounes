package dev.lefredy.mestounes.viewModel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.simplecityapps.ktaglib.KTagLib
import dev.lefredy.mestounes.core.MusicMediadata
import java.io.File

class Directory (
    val musics: List<String>,
    val folders: List<String>
)

class LocalMusicViewModel {
    companion object {
        var instance: LocalMusicViewModel? = null

        fun require(): LocalMusicViewModel {
            if (instance == null) {
                instance = LocalMusicViewModel()
            }
            return instance!!
        }
    }

    val cachedFolders: MutableMap<String, Directory> = mutableMapOf()

    fun cacheFolder(path: String, folders: List<String>, musics: List<String>) {
        cachedFolders[path] = Directory(musics.toList(), folders.toList())
    }

    val library: MutableMap<String, MediaItem> = mutableMapOf()

    fun register( context: Context, path: String ): Boolean { return register(context, File(path)) }
    fun register( context: Context, file: File ): Boolean {
        val uri = Uri.fromFile(file)
        if (uri.toString() in library.keys) return true
        // TODO: CHANGE THIS BULLSHIT
        if ( !file.exists() || file.length() == 0L ) {
            Log.d("LocalMusicViewModel", "Ignored nonexisting file : ${file.path}")
            return false
        }
        if ( !file.isFile ) {
            Log.d("LocalMusicViewModel", "Ignored folder : ${file.path}")
            return false
        }
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
            retriever.release()
            if ( hasAudio != "yes" ) {
                Log.d("LocalMusicViewModel", "Ignored non-audio file : ${file.path}")
                return false
            }
        } catch (_: Exception) {
            try { retriever.release() } catch (_: Exception) {}
            Log.d("LocalMusicViewModel", "Ignored file due to error reading metadata : ${file.path}")
            return false
        }
        val metadata = extractTags(context, uri, file.path)
        val mediaMetadata = MediaMetadata
            .Builder()
            .setTitle(metadata.title)
            .setArtist(metadata.readableArtists)
            .setAlbumArtist(metadata.readableAlbumArtists)
            .setAlbumTitle(metadata.album)
            .setTrackNumber(metadata.trackNumber)
            .setDiscNumber(metadata.discNumber)
            .build()
        library[uri.toString()] = MediaItem
            .Builder()
            .setUri(uri)
            .setMediaId(uri.toString())
            .setMediaMetadata(mediaMetadata)
            .build()
        return true
    }

    fun extractTags(context: Context, uri: Uri, path: String): MusicMediadata {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val tags = KTagLib().getMetadata(pfd.detachFd(), uri.path)
            if (tags != null) {
                return MusicMediadata(tags.propertyMap, uri.lastPathSegment ?: "unknown")
            }
        }
        return MusicMediadata(mapOf<String, List<String>>(), path)
    }

//    fun getLibraryIdFromUri(uri: Uri?): Int {
//        if (uri == null) return -1
//        for ((id, music) in library) {
//            if (music.uri == uri) {
//                return id
//            }
//        }
//        return -1
//    }

    fun loadLibraryFromCache() {

    }

    fun fromUri(context: Context, uri: Uri): String {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val metadata = KTagLib().getMetadata(pfd.detachFd(), uri.path)
                if (metadata != null) {
                    var out = ""
                    for (tag in metadata.propertyMap) {
                        out += "'${tag.key}': " + tag.value.toString() + "\n"
                    }
                    return out
                } else {
                    return "reading tags error"
                }
            }
            return "null"
        } catch (e: Exception) {
            e.printStackTrace()
            return "null"
        }
    }
}
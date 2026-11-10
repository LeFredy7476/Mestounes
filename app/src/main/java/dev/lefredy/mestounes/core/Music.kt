package dev.lefredy.mestounes.core

import android.content.Context
import android.net.Uri
import android.util.Log
import com.simplecityapps.ktaglib.KTagLib


object TagNames {
    const val DATE = "DATE"
    const val ARTIST = "ARTIST"
    const val ALBUM = "ALBUM"
    const val GENRE = "GENRE"
    const val TITLE = "TITLE"
    const val TRACKNUMBER = "TRACKNUMBER"
    const val ALBUMARTIST = "ALBUMARTIST"
    const val DISCNUMBER = "DISCNUMBER"
    const val LABEL = "LABEL"
    const val LENGTH = "LENGTH"
    const val ENCODING = "ENCODING"
    const val COMMENT = "COMMENT"
    const val TRACKTOTAL = "TRACKTOTAL"
    const val DISCTOTAL = "DISCTOTAL"
}

class MusicMediadata {
    val title: String
    val artists: List<String>
    val albumArtists: List<String>
    val album: String
//    val genres: List<String>
//    val date: Int
//    val comments: List<String>
    val trackNumber: Int
    val discNumber: Int

    constructor(id3v2: Map<String, List<String>>, filename: String) {
        title = id3v2[TagNames.TITLE]?.get(0) ?: filename.reversed().split('.', limit = 2).last().reversed()
        artists = id3v2[TagNames.ARTIST] ?: listOf("Unknown")
        albumArtists = id3v2[TagNames.ALBUMARTIST] ?: listOf("Unknown")
        album = id3v2[TagNames.ALBUM]?.get(0) ?: "Unknown" // To Be Changed (look at folder to default album name)
        trackNumber = id3v2[TagNames.TRACKNUMBER]?.get(0)?.split("/")?.first()?.toInt() ?: 0
        discNumber = id3v2[TagNames.DISCNUMBER]?.get(0)?.split("/")?.first()?.toInt() ?: 0
    }

    val readableArtists: String
        get() = artists.joinToString(", ")
    val readableAlbumArtists: String
        get() = albumArtists.joinToString(", ")
}

class Music (
    val local: Boolean,
    val uri: Uri
) {
    var metadata: MusicMediadata? = null

    fun loadMetadataFromFile(context: Context) {
        Log.d("MainActivity", "loading file $uri")
        if (local) {
//            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val tags = KTagLib().getMetadata(pfd.detachFd(), uri.path)
                    if (tags != null) {
                        metadata = MusicMediadata(tags.propertyMap, uri.lastPathSegment ?: "unknown")
                    }
                }
//            } catch (e: Exception) {
//                Log.e("MainActivity", "Error while loading $uri")
//                e.printStackTrace()
//            }
        } else {
            // TODO
        }
    }

    fun obtainMetadata (context: Context): MusicMediadata {
        if (metadata == null) {
            loadMetadataFromFile(context)
        }
        return metadata!!
    }
}

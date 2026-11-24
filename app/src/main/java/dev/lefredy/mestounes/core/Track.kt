package dev.lefredy.mestounes.core

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.text.isDigitsOnly
import com.simplecityapps.ktaglib.KTagLib
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import java.io.File

class Track private constructor (
    val path: String, // PRIMARY KEY, relative path to the root folder
    var trackId: Long? = null, // LEGACY, server id (null means local only)
    var title: String, // if missing, default to filename
    val mediaLength: Long, // in millisecond
    var artists: MutableList<Artist>, // if empty, shows "unknown" in UI
    var album: String, // if missing, default to parent directory name
    var albumArtist: Artist, // if missing, default to first item in artist list if available.
    var composer: String? = null, // if missing, default to albumArtist
    var date: Int? = null, // if null, shows "unknown" in UI
    var genres: MutableSet<Genre>? = null, // if empty, shows "unknown" in UI (null = not fetched yet)
    var trackNumber: Int = 0, // if missing, default to 0
    var discNumber: Int = 0, // if missing, default to 0
    var comment: String = "", // can be empty. PS: comments are static to the original file and not user-defined
) {
    constructor (trackEntity: TrackWithArtist): this (
        trackEntity.track.path,
        trackEntity.track.trackId,
        trackEntity.track.title,
        trackEntity.track.mediaLength,
        trackEntity.artists.map(::Artist).toMutableList(),
        trackEntity.track.album,
        Artist(trackEntity.track.albumArtist),
        trackEntity.track.composer,
        trackEntity.track.date,
        null,
        trackEntity.track.trackNumber,
        trackEntity.track.discNumber,
        trackEntity.track.comment,
    )

    companion object {
        // WILL TAKE TIME
        // CAN FAIL, MUST FIX BEFORE FINAL
        fun fromMetadata(context: Context, uri: Uri): Track {
            // TODO: add fallback to catch errors

            val file = uri.toFile()
            val libraryFolder = File("/storage/emulated/0/Music") // TODO: link to actual setting
            var id3v2: Map<String, List<String>> = mapOf()
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val tags = KTagLib().getMetadata(pfd.detachFd(), uri.path)
                if (tags != null) id3v2 = tags.propertyMap
            }
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val mediaLength: Long = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            val path: String = file.relativeTo(libraryFolder).path
            val title: String =
                id3v2["TITLE"]?.getOrNull(0)?.trim() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim() ?:
                file.path.split("/").last().reversed().split(".", limit = 2).last().reversed()
            val artists: List<String> =
                id3v2["ARTIST"]?.let { if (it.size == 1) it[0].split(",", ";", "feat.").map(String::trim) else it } ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.split(",", ";", "feat.")?.map(String::trim) ?:
                listOf("Unknown")
            val album: String =
                id3v2["ALBUM"]?.getOrNull(0)?.trim() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.trim() ?:
                "Unknown"
            val albumArtist: String =
                id3v2["ALBUMARTIST"]?.getOrNull(0)?.trim() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)?.trim() ?:
                artists.getOrNull(0) ?:
                "Unknown"
            val composer: String? =
                id3v2["COMPOSER"]?.getOrNull(0)?.trim() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)?.trim()
            val date: Int? =
                id3v2["DATE"]?.joinToString("-")?.trim()?.split("-", ".", " ")?.firstOrNull{it.isDigitsOnly() && it.length == 4}?.toInt() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.trim()?.split("-", ".", " ")?.firstOrNull{it.isDigitsOnly() && it.length == 4}?.toInt() ?:
                id3v2["YEAR"]?.getOrNull(0)?.trim()?.toInt() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.trim()?.toInt()
            val genres: List<String> =
                id3v2["GENRE"]?.map(String::lowercase)?.map(String::trim)?.let { if (it.size == 1) it[0].split(",", ";", "feat.").map(String::trim) else it } ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.lowercase()?.split(",", ";", "feat.")?.map(String::trim) ?:
                listOf()
            val trackNumber: Int =
                id3v2["TRACKNUMBER"]?.getOrNull(0)?.split("/")?.firstOrNull()?.toInt() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toInt() ?:
                0
            val discNumber: Int =
                id3v2["DISCNUMBER"]?.getOrNull(0)?.split("/")?.firstOrNull()?.toInt() ?:
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)?.toInt() ?:
                0
            val comment: String =
                id3v2["COMMENT"]?.joinToString("; ")?.replace("\n", " ") ?:
                ""

            return Track(
                path, null, title, mediaLength,
                artists.map(::Artist).toMutableList(),
                album, Artist(albumArtist), composer, date,
                genres.map{Genre(it, null)}.toMutableSet(),
                trackNumber, discNumber, comment
            )
        }
    }

    fun toTrackEntity() = TrackEntity(
        path, trackId, title, mediaLength, album, albumArtist.name,
        composer, date, trackNumber, discNumber, comment,
    )

    suspend fun fetchGenres(): Track {
        genres = genres ?: LocalLibrary.require().getTrackGenres(this).toMutableSet()
        return this
    }

    suspend fun save() {
        LocalLibrary.require().saveTrack(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false
        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
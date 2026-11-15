package dev.lefredy.mestounes.core

import dev.lefredy.mestounes.registry.LibraryDatabase
import dev.lefredy.mestounes.registry.entity.ArtistEntity
import dev.lefredy.mestounes.registry.entity.GenreEntity
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import dev.lefredy.mestounes.registry.entity.TrackWithArtistAndGenre

class Track private constructor(
    val path: String, // PRIMARY KEY, relative path to the root folder
    var trackId: Long? = null, // server id, (null means local only)
    var title: String, // if missing, default to filename
    var artists: MutableList<String>, // if empty, shows "unknown" in UI
    var album: String, // if missing, default to parent directory name
    var albumArtist: String?, // if missing, default to first item in artist list if available.
    var composer: String? = null, // if missing, default to albumArtist
    var date: Int? = null, // if null, shows "unknown" in UI
    var genres: MutableSet<String>?, // if empty, shows "unknown" in UI (null = not fetched yet)
    var trackNumber: Int = 0, // if missing, default to 0
    var discNumber: Int = 0, // if missing, default to 0
    var comment: String = "", // can be empty. PS: comments are static to the original file and not user-defined
) {

    companion object {

        /*fun importRoomEntity(trackEntity: TrackWithArtistAndGenre): Track {
            return Track(
                trackEntity.track.path,
                trackEntity.track.trackId,
                trackEntity.track.title,
                trackEntity.artists.map{it.name}.toMutableList(),
                trackEntity.track.album,
                trackEntity.track.albumArtist,
                trackEntity.track.composer,
                trackEntity.track.date,
                trackEntity.genres.map{it.name}.toMutableSet(),
                trackEntity.track.trackNumber,
                trackEntity.track.discNumber,
                trackEntity.track.comment,
            )
        }*/

        fun importRoomEntity(trackEntity: TrackWithArtist): Track {
            return Track(
                trackEntity.track.path,
                trackEntity.track.trackId,
                trackEntity.track.title,
                trackEntity.artists.map{it.name}.toMutableList(),
                trackEntity.track.album,
                trackEntity.track.albumArtist,
                trackEntity.track.composer,
                trackEntity.track.date,
                null,
                trackEntity.track.trackNumber,
                trackEntity.track.discNumber,
                trackEntity.track.comment,
            )
        }
    }

    suspend fun fetchGenre(libraryDatabase: LibraryDatabase): Track {
        genres = genres ?: libraryDatabase.genreDao().getTrackGenres(path).map{it.name}.toMutableSet()
        return this
    }

    suspend fun saveToDatabase(libraryDatabase: LibraryDatabase) {
        libraryDatabase.trackDao().insertTrack(
            TrackEntity(
                path,
                trackId,
                title,
                album,
                albumArtist,
                composer,
                date,
                trackNumber,
                discNumber,
                comment,
            )
        )

        // clean and repopulate artists references
        libraryDatabase.artistDao().deleteTrackArtists(path)
        artists.forEachIndexed { index, name ->
            libraryDatabase.artistDao().insertArtist(ArtistEntity(path, name, index.toByte()))
        }

        // repopulate genres and discard leftovers
        genres?.forEach {
            libraryDatabase.genreDao().insertGenre(GenreEntity(path, it))
        }
        genres?.let {
            libraryDatabase.genreDao().deleteOldTrackGenres(path, it)
        }
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
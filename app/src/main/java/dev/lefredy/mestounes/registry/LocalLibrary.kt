package dev.lefredy.mestounes.registry

import androidx.room3.Room
import dev.lefredy.mestounes.core.*
import dev.lefredy.mestounes.registry.entity.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@JvmName("genreFromMap")
private fun GenreAsync(emission: Map<String, Int>) = emission.map(::Genre)
@JvmName("genreFromGenreEntity")
private fun GenreAsync(emission: Iterable<GenreEntity>) = emission.map(::Genre)
@JvmName("trackFromTrackWithArtist")
private fun TrackAsync(emission: Iterable<TrackWithArtist>) = emission.map(::Track)
@JvmName("artistFromArtistItem")
private fun ArtistAsync(emission: Iterable<ArtistItem>) = emission.map(::Artist)
@JvmName("artistFromArtistEntity")
private fun ArtistAsync(emission: Iterable<ArtistEntity>) = emission.map(::Artist)
@JvmName("artistFromString")
private fun ArtistAsync(emission: Iterable<String>) = emission.map(::Artist)
@JvmName("albumFromAlbumEntity")
private fun AlbumAsync(emission: Iterable<AlbumEntity>) = emission.map(::Album)
@JvmName("directoryFromDirectoryEntity")
private fun DirectoryAsync(emission: Iterable<DirectoryEntity>) = emission.map(::Directory)

@OptIn(ExperimentalCoroutinesApi::class)
class LocalLibrary private constructor(
    private val libraryDatabase: LibraryDatabase
) {
    companion object {
        private val instanceDeferred = CompletableDeferred<LocalLibrary>()
        fun require(context: android.content.Context): LocalLibrary {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val localLibrary = LocalLibrary(Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library.db"
                ).build())
                instanceDeferred.complete(localLibrary)
                return localLibrary
            }
        }
        suspend fun require(): LocalLibrary {
            return instanceDeferred.await()
        }
        fun get(): LocalLibrary {
            return instanceDeferred.getCompleted()
        }
    }

    suspend fun saveTrack(track: Track) {
        libraryDatabase.trackDao().upsert(track.toTrackEntity())
        // clean and repopulate artists references
        libraryDatabase.artistDao().deleteFromTrack(track.path)
        track.artists.forEachIndexed { index, artist -> libraryDatabase.artistDao().upsert(artist.toArtistEntity(track.path, index)) }
        // repopulate genres and discard leftovers
        track.genres?.forEach { libraryDatabase.genreDao().insert(GenreEntity(track.path, it.name)) }
        track.genres?.let { libraryDatabase.genreDao().deleteUnusedFromTrack(track.path, it.map{g->g.name}.toSet()) }
    }
    
    suspend fun deleteTrack(track: Track) {
        libraryDatabase.trackDao().delete(track.toTrackEntity())
        libraryDatabase.genreDao().deleteFromTrack(track.path)
        libraryDatabase.artistDao().deleteFromTrack(track.path)
    }

    suspend fun getTracks() = libraryDatabase.trackDao().getAll().map(::Track)
    fun getTracksAsync() = libraryDatabase.trackDao().getAllAsync().map(::TrackAsync)

    suspend fun getTrack(path: String) = libraryDatabase.trackDao().get(path)?.let(::Track)
    fun getTrackAsync(path: String) = libraryDatabase.trackDao().getAsync(path).map{it?.let(::Track)}

    suspend fun getTrackRaw(path: String) = libraryDatabase.trackDao().getRaw(path)?.let(::Track)
    fun getTrackRawAsync(path: String) = libraryDatabase.trackDao().getRawAsync(path).map{it?.let(::Track)}

    suspend fun getTracksByAlbum(album: String, albumArtist: String) = libraryDatabase.trackDao().getByAlbumOrdered(album, albumArtist).map(::Track)
    fun getTracksByAlbumAsync(album: String, albumArtist: String) = libraryDatabase.trackDao().getByAlbumOrderedAsync(album, albumArtist).map(::TrackAsync)

    suspend fun getTracksByAlbum(album: Album) = getTracksByAlbum(album.name, album.albumArtist.name)
    fun getTracksByAlbumAsync(album: Album) = getTracksByAlbumAsync(album.name, album.albumArtist.name)

    suspend fun getAlbumsByAlbumArtist(albumArtist: Artist) = libraryDatabase.albumDao().getByAlbumArtist(albumArtist.name).map(::Album)
    fun getAlbumsByAlbumArtistAsync(albumArtist: Artist) = libraryDatabase.albumDao().getByAlbumArtistAsync(albumArtist.name).map(::AlbumAsync)

    suspend fun getAlbums() = libraryDatabase.albumDao().getAll().map(::Album)
    fun getAlbumsAsync() = libraryDatabase.albumDao().getAllAsync().map(::AlbumAsync)

    suspend fun getAlbum(name: String, albumArtist: Artist) = Album(libraryDatabase.albumDao().get(name, albumArtist.name))
    fun getAlbumAsync(name: String, albumArtist: Artist) = libraryDatabase.albumDao().getAsync(name, albumArtist.name).map(::Album)

    suspend fun getArtists() = libraryDatabase.artistDao().getAll().map(::Artist)
    fun getArtistsAsync() = libraryDatabase.artistDao().getAllAsync().map(::ArtistAsync)

    suspend fun getArtistsFromTrack(track: Track) = libraryDatabase.artistDao().getFromTrack(track.path).map(::Artist)
    fun getArtistsFromTrackAsync(track: Track) = libraryDatabase.artistDao().getFromTrackAsync(track.path).map(::ArtistAsync)

    suspend fun getAlbumArtists() = libraryDatabase.albumDao().getArtists().map(::Artist)
    fun getAlbumArtistsAsync() = libraryDatabase.albumDao().getArtistsAsync().map(::ArtistAsync)

    suspend fun getEveryArtists() = libraryDatabase.artistDao().getAll().union(libraryDatabase.albumDao().getArtists()).map(::Artist)
    fun getEveryArtistsAsync() = combine(libraryDatabase.artistDao().getAllAsync(), libraryDatabase.albumDao().getArtistsAsync()){a,b->a.union(b)}.map(::ArtistAsync)

    suspend fun getTracksByArtist(artist: Artist) = libraryDatabase.trackDao().getByArtist(artist.name).map(::Track)
    fun getTracksByArtistAsync(artist: Artist) = libraryDatabase.trackDao().getByArtistAsync(artist.name).map(::TrackAsync)

    suspend fun getGenres() = libraryDatabase.genreDao().getAll().map(::Genre)
    fun getGenresAsync() = libraryDatabase.genreDao().getAllAsync().map(::GenreAsync)

    suspend fun getTrackGenres(track: Track) = libraryDatabase.genreDao().getFromTrack(track.path).map(::Genre).toSet()
    fun getTrackGenresAsync(track: Track) = libraryDatabase.genreDao().getFromTrackAsync(track.path).map(::GenreAsync).map{it.toSet()}

    suspend fun getTracksByGenre(genre: Genre) = libraryDatabase.trackDao().getByGenre(genre.name).map(::Track)
    fun getTracksByGenreAsync(genre: Genre) = libraryDatabase.trackDao().getByGenreAsync(genre.name).map(::TrackAsync)

    suspend fun getDirectories() = libraryDatabase.directoryDao().getAll().map(::Directory)
    fun getDirectoriesAsync() = libraryDatabase.directoryDao().getAllAsync().map(::DirectoryAsync)

    suspend fun getTracksByDirectory(directory: Directory) = libraryDatabase.trackDao().getByDirectory(directory.directory).map(::Track)
    fun getTracksByDirectoryAsync(directory: Directory) = libraryDatabase.trackDao().getByDirectoryAsync(directory.directory).map(::TrackAsync)


    suspend fun insertPlaylist(playlist: Playlist): Playlist {
        return Playlist(libraryDatabase.playlistDao().insertPlaylist(playlist.toPlaylistEntity()))
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        if (playlist.id == null) return
        libraryDatabase.playlistDao().updatePlaylist(playlist.toPlaylistEntity())
    }

    suspend fun getPlaylists() = libraryDatabase.playlistDao().getAll().map(::Playlist)

    suspend fun getPlaylistTracks(playlist: Playlist) = playlist.id?.let{libraryDatabase.playlistDao().getPlaylistTracks(it).map(::Track)} ?: listOf()

    suspend fun fillPlaylist(playlist: Playlist) {
        if (playlist.id == null) return
        libraryDatabase.playlistDao().clearPlaylist(playlist.id!!)
        libraryDatabase.playlistDao().fillPlaylist(playlist.tracks.mapIndexed { index, track -> PlaylistItem(playlist.id!!, track.path, index.toShort()) })
    }
}
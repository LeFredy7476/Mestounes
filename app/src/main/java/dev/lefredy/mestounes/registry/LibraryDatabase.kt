package dev.lefredy.mestounes.registry

import androidx.room3.Database
import androidx.room3.RoomDatabase
import dev.lefredy.mestounes.registry.dao.AlbumDao
import dev.lefredy.mestounes.registry.dao.ArtistDao
import dev.lefredy.mestounes.registry.dao.DirectoryDao
import dev.lefredy.mestounes.registry.dao.GenreDao
import dev.lefredy.mestounes.registry.dao.PlaylistDao
import dev.lefredy.mestounes.registry.dao.TrackDao
import dev.lefredy.mestounes.registry.entity.*

@Database(
    version = 1,
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
        PlaylistEntity::class,
        PlaylistItem::class,
    ],
    exportSchema = false,
)
internal abstract class LibraryDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun genreDao(): GenreDao
    abstract fun artistDao(): ArtistDao
    abstract fun directoryDao(): DirectoryDao
    abstract fun playlistDao(): PlaylistDao
}
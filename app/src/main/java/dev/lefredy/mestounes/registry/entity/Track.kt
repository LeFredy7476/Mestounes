package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
        ForeignKey(Artist::class, ["artistId"], ["albumArtist"]),
//        ForeignKey(Source::class, ["sourceId"], ["sourceId"]),
//        ForeignKey(Album::class, ["albumId"], ["album"]),
    ]
)
data class Track (
    @PrimaryKey val trackId: Long,
//    val sourceId: Long, // refer to the source used
    val distantId: Long?, // server id, (null means local only)
    @ColumnInfo(index = true) val path: String,
    val title: String,
    val album: String,
    val albumArtist: Long?,
    val composer: String?,
    val date: Int?,
    val trackNumber: Int,
    val discNumber: Int,
    val comment: String,
)

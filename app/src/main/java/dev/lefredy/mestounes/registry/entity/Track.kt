package dev.lefredy.mestounes.registry.entity

import androidx.room3.*

@Entity(
    foreignKeys = [
        ForeignKey(Artist::class, ["artistId"], ["albumartist"]),
        ForeignKey(Source::class, ["sourceId"], ["sourceId"]),
//        ForeignKey(Album::class, ["albumId"], ["album"]),
    ]
)
data class Track (
    @PrimaryKey val trackId: Long,
    val sourceId: Long, // refer to the source used
    val distantId: Long, // works within the concerned source
    @ColumnInfo(index = true) val path: String,
    val title: String,
    val album: String,
    val albumartist: Long?,
    val composer: String?,
    val date: Int?,
    val tracknumber: Int,
    val discnumber: Int,
    val comment: String,
)

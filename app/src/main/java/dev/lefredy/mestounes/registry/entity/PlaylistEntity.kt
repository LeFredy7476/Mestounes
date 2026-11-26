package dev.lefredy.mestounes.registry.entity

import androidx.room3.*
import java.util.UUID

@Entity(
    tableName = "playlists",
    indices = [Index(value = ["playlistRef"], unique = true)]
)
data class PlaylistEntity (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val name: String,
    val owned: Boolean,
    val playlistRef: UUID?, // null if local only
    val updatedAt: Long,
    val deleted: Boolean,
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["path", "id", "index"],
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(PlaylistEntity::class, ["id"], ["id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TrackEntity::class, ["path"], ["path"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PlaylistItem(
    val id: Long,
    val path: String,
    val index: Short,
)

//data class PlaylistWithTracks(
//    @Embedded val playlist: PlaylistEntity,
//    @Relation(
//        parentColumns = ["id"],
//        entityColumns = ["path"],
//        associateBy = Junction(PlaylistItem::class)
//    )
//    val tracks: List<TrackWithArtist>
//)
package dev.lefredy.mestounes.modules

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dev.lefredy.mestounes.core.Track
import dev.lefredy.mestounes.utils.Event0
import dev.lefredy.mestounes.utils.Event1
import dev.lefredy.mestounes.utils.Event2
import dev.lefredy.mestounes.utils.Event3

class MediaPlayer private constructor() {

    private var player: Player? = null
    private var listener: Player.Listener? = null

    val onPlayerReady = Event0()
    val onIsPlayingChanged = Event1<Boolean>()
    val onPlayWhenReadyChanged = Event2<Boolean, Int>()
    val onPositionDiscontinuity = Event3<Player.PositionInfo, Player.PositionInfo, Int>()
    val onMediaMetadataChanged = Event1<MediaMetadata>()

    companion object {

        const val OK = 0
        const val NOT_INITIALIZED = 1

        private var instance: MediaPlayer? = null

        fun require(): MediaPlayer {
            if (instance == null) instance = MediaPlayer()
            return instance!!
        }
    }

    init {
        // immediately execute function if the exoplayer is already loaded
        onPlayerReady.filter = fun (function: () -> Unit): Boolean {
            if (player == null) {
                return true
            } else {
                function()
                return false
            }
        }
    }

    fun init(_player: Player): () -> Unit {
        player = _player
        val _listener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                onPlayWhenReadyChanged.call(playWhenReady, reason)
            }
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                onPositionDiscontinuity.call(oldPosition, newPosition, reason)
            }
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                onMediaMetadataChanged.call(mediaMetadata)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                onIsPlayingChanged.call(isPlaying)
            }
        }
        _player.addListener(_listener)
        listener = _listener
        onPlayerReady.call()
        return {
            player = null
        }
    }

    suspend fun loadQueue(tracklist: List<Track>, at: Int, ms: Long): Int {
        val _player = player ?: return NOT_INITIALIZED
        val fileProvider = FileProvider.require()
        val mediaItemList = tracklist.map {
            val metadata = MediaMetadata.Builder()
                .setTitle(it.title)
                .setArtist(it.artistsString())
                .setAlbumTitle(it.album)
                .setAlbumArtist(it.albumArtist.name)
                .setComposer(it.composer)
                .setGenre(it.genreString())
                .setTrackNumber(it.trackNumber)
                .setDiscNumber(it.discNumber)
                .setReleaseYear(it.date)
                .setDescription(it.comment)
                .build()
            MediaItem.Builder()
                .setMediaId(it.path)
                .setUri(fileProvider.getTrackURI(it))
                .setMediaMetadata(metadata)
                .setTag(Track)
                .build()
        }
        _player.setMediaItems(mediaItemList, at, ms)
        return OK
    }

    fun executePlay(): Int {
        val _player = player ?: return NOT_INITIALIZED
        _player.play()
        return OK
    }
    fun executePause(): Int {
        val _player = player ?: return NOT_INITIALIZED
        _player.pause()
        return OK
    }
    fun executeNext(): Int {
        val _player = player ?: return NOT_INITIALIZED
        _player.seekToNext()
        return OK
    }
    fun executePrev(): Int {
        val _player = player ?: return NOT_INITIALIZED
        _player.seekToPrevious()
        return OK
    }
    fun executeSeek(position: Long): Int {
        val _player = player ?: return NOT_INITIALIZED
        _player.seekTo(position)
        return OK
    }

    fun getCurrentDuration() = player?.duration ?: 0L

    fun getCurrentPosition() = player?.currentPosition ?: 0L

    fun getCurrentTrack() : Track? = player?.currentMediaItem?.localConfiguration?.tag as? Track
}
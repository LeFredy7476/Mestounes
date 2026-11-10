package dev.lefredy.mestounes.viewModel
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dev.lefredy.mestounes.utils.*


class PlayerViewModel {
    companion object {
        var instance: PlayerViewModel? = null

        fun require(): PlayerViewModel {
            if (instance == null) {
                instance = PlayerViewModel()
            }
            return instance!!
        }
    }

    private var player: Player? = null
    private var listener: Player.Listener? = null


    val playWhenReady: Boolean
        get() = player?.playWhenReady ?: false

    val isPlaying: Boolean
        get() = player?.isPlaying ?: false

    val onPlayerReady = Event0()
    val onIsPlayingChanged = Event1<Boolean>()
    val onPlayWhenReadyChanged = Event2<Boolean, Int>()
    val onPositionDiscontinuity = Event3<Player.PositionInfo, Player.PositionInfo, Int>()
    val onMediaMetadataChanged = Event1<MediaMetadata>()

    fun setupPlayer(player: Player) {
        this.player = player
        val listener = object : Player.Listener {
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
        player.addListener(listener)
        this.listener = listener
        onPlayerReady.call()
    }

    fun clearView() {
        onPlayerReady.clear()
        onIsPlayingChanged.clear()
        onMediaMetadataChanged.clear()
        onPlayWhenReadyChanged.clear()
        onPositionDiscontinuity.clear()
    }

    fun clearPlayer() {
        listener?.let {
            player?.removeListener(it)
        }
        player = null
    }

    fun play() = player?.play()
    fun stop() = player?.pause()
    fun skip() = player?.seekToNext()
    fun prev() = player?.seekToPrevious()

    fun loadSingleUri(uri: Uri) {
        if (player == null) return
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(uri.toString())
            .build()
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun loadSingleMusic(uri: Uri) {loadSingleMusic(uri.toString())}
    fun loadSingleMusic(key: String) {
        if (player == null) return
        val localMusicViewModel = LocalMusicViewModel.require()
        val mediaItem = localMusicViewModel.library[key] ?: return
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun loadPlaylist(mediaitems: List<MediaItem>, startIndex: Int = 0) {
        player!!.setMediaItems(mediaitems, startIndex, 0L)
        player!!.prepare()
        player?.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun getCurrentMediaItem() : MediaItem? = player?.currentMediaItem
    fun getCurrentMediaId() : String? = player?.currentMediaItem?.mediaId

    val duration: Long
        get() = player?.duration ?: 0L

    val currentPosition: Long
        get() = player?.currentPosition ?: 0L

    fun seekTo(position: Long) {
        player?.seekTo(position)
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
}
package dev.lefredy.mestounes

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import dev.lefredy.mestounes.registry.LibrairyDatabase
import dev.lefredy.mestounes.viewModel.PlayerViewModel


class MediaService : MediaSessionService() {

    private val playerViewModel = PlayerViewModel.require()
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {

        Log.d("MediaService", "onCreate called")

        val librairyDatabase = LibrairyDatabase.require(applicationContext)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        super.onCreate()
        val player = ExoPlayer
            .Builder(this)
            .build()
            .apply {
                setAudioAttributes(audioAttributes, true) // 'true' handles audio focus automatically
                setHandleAudioBecomingNoisy(true) // Pauses playback if headphones are unplugged
            }
        exoPlayer = player
        mediaSession = MediaSession.Builder(this, player).build()
        playerViewModel.setupPlayer(player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {

        Log.d("MediaService", "onDestroy called")

        playerViewModel.clearPlayer()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        exoPlayer = null
        super.onDestroy()
    }
}
package dev.lefredy.mestounes.modules

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
class MediaPlayer private constructor() {

    var player: androidx.media3.common.Player? = null

    companion object {

        const val CODE_OK = 0
        const val CODE_NOT_INITIALIZED = 1

        private var instance: MediaPlayer? = null

        fun require(): MediaPlayer {
            if (instance == null) instance = MediaPlayer()
            return instance!!
        }
    }

    fun init(_player: androidx.media3.common.Player): () -> Unit {
        player = _player
        return {
            player = null
        }
    }

    fun executePlay(): Int {
        if (player == null) return CODE_NOT_INITIALIZED
        return CODE_OK
    }
    fun executeStop(): Int {
        if (player == null) return CODE_NOT_INITIALIZED
        return CODE_OK
    }
    fun executeNext(): Int {
        if (player == null) return CODE_NOT_INITIALIZED
        return CODE_OK
    }
    fun executePrev(): Int {
        if (player == null) return CODE_NOT_INITIALIZED
        return CODE_OK
    }

}
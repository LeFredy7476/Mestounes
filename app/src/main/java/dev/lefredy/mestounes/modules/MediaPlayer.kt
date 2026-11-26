package dev.lefredy.mestounes.modules

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
class MediaPlayer private constructor(
    private val player: androidx.media3.common.Player
){

    companion object {
        private val instanceDeferred = CompletableDeferred<MediaPlayer>()

        fun init(player: androidx.media3.common.Player): MediaPlayer {
            try {
                return instanceDeferred.getCompleted()
            } catch (_: Exception) {
                val fileProvider = MediaPlayer(player)
                instanceDeferred.complete(fileProvider)
                return fileProvider
            }
        }

        suspend fun require(): MediaPlayer {
            return instanceDeferred.await()
        }
    }

    fun executePlay() {

    }
    fun executeStop() {

    }
    fun executeNext() {

    }
    fun executePrev() {
        
    }

}
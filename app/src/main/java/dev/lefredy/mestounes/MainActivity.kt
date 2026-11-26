package dev.lefredy.mestounes

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dev.lefredy.mestounes.modules.FileProvider
import dev.lefredy.mestounes.modules.MediaPlayer
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.ui.MainView

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var localLibrary: LocalLibrary? = null
    private var fileProvider: FileProvider? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")

        mediaPlayer = MediaPlayer.require()
        localLibrary = LocalLibrary.require(applicationContext)
        fileProvider = FileProvider.require(applicationContext)

        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        fileProvider?.onStartup() // remove if any casualties
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        LocalLibrary.require(applicationContext)
        FileProvider.require(applicationContext)
        enableEdgeToEdge()
        setContent { MainView() }
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop called")
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy called")
        mediaPlayer?.purgeListeners()
        super.onDestroy()
    }
}
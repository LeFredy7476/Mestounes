package dev.lefredy.mestounes

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dev.lefredy.mestounes.registry.LibrairyDatabase
import dev.lefredy.mestounes.ui.MainView
import dev.lefredy.mestounes.viewModel.PlayerViewModel

class MainActivity : ComponentActivity() {

    val playerViewModel = PlayerViewModel.require()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")
        val librairyDatabase = LibrairyDatabase.require(applicationContext)

        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture?.addListener({
//            controller?.let {
////                playerViewModel.setupPlayer(it)
//                Log.d("MainActivity", "controller loaded")
//            }
//        }, MoreExecutors.directExecutor())
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
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
        playerViewModel.clearView()
        super.onDestroy()
    }
}
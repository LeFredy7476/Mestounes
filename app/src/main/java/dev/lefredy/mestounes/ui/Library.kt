package dev.lefredy.mestounes.ui

import android.content.Context
import java.io.File
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.lefredy.mestounes.viewModel.LocalMusicViewModel
import dev.lefredy.mestounes.viewModel.PlayerViewModel
import kotlinx.serialization.Serializable


val LIST_ITEM_SHAPE = RoundedCornerShape(8.dp)
val TRANSITION_TWEEN_DURATION = 400

@Serializable
object HomeTab

@Serializable
object ArtistTab

@Serializable
object AlbumTab

@Serializable
object TrackTab

@Serializable
object LocalFolderTab {
    interface ListItem {
        val isFolder: Boolean
        val path: String
        fun queueUp(queue: MutableList<MediaItem>, startindex: Int, index: Int): Int {
            return if (index < startindex) startindex - 1
            else startindex
        }
    }

    class MusicListItem(
        val uri: Uri,
        override val path: String
    ): ListItem {
        override val isFolder = false
        override fun queueUp(queue: MutableList<MediaItem>, startindex: Int, index: Int): Int {
            val localMusicViewModel = LocalMusicViewModel.require()
            val mediaItem = localMusicViewModel.library[uri.toString()]
            return mediaItem?.let {
                if (queue.add(mediaItem)) startindex else null
            } ?: super.queueUp(queue, startindex, index)
        }
    }

    class FolderListItem(
        override val path: String
    ): ListItem {
        override val isFolder = true
    }

    @Serializable
    object RootFolder

    @Serializable
    data class Folder(val folder: String)
}



fun folderWalker(context: Context, folder: File, folderList: MutableList<String>, fileList: MutableList<String>) {
    val folderContent = folder.listFiles()!!.toList()
    for (item in folderContent) {
        if (item.isDirectory) {
            folderList.add(item.path)
        } else {
            if (LocalMusicViewModel.require().register(context, item)) fileList.add(Uri.fromFile(item).toString())
        }
    }
}


@Composable
fun MusicListItem(
    key: String,
    nextKey: String? = null,
    index: Int,
    isLast: Boolean = false,
    onPlay: () -> Unit
) {
    val playerViewModel = PlayerViewModel.require()
    val localMusicViewModel = LocalMusicViewModel.require()
    var isMusicPlaying by remember { mutableStateOf(playerViewModel.getCurrentMediaId() == key) }
    var isNextMusicPlaying by remember { mutableStateOf(playerViewModel.getCurrentMediaId() == nextKey) }
    playerViewModel.onMediaMetadataChanged += { _ ->
        if (playerViewModel.getCurrentMediaId() == key && !isMusicPlaying) {
            isMusicPlaying = true
        } else if (playerViewModel.getCurrentMediaId() != key && isMusicPlaying) {
            isMusicPlaying = false
        }
        if (playerViewModel.getCurrentMediaId() == nextKey && !isNextMusicPlaying) {
            isNextMusicPlaying = true
        } else if (playerViewModel.getCurrentMediaId() != nextKey && isNextMusicPlaying) {
            isNextMusicPlaying = false
        }
    }

    val paddingLeft by animateFloatAsState(
        targetValue = if (isMusicPlaying) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutElastic), // Optional customization
        label = "itemPaddingLeft"
    )

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(LIST_ITEM_SHAPE)
            .clickable {
                onPlay()
            }
            .let {
                if (isMusicPlaying) it
                    .border(2.dp, MaterialTheme.colorScheme.primary, LIST_ITEM_SHAPE)
                    .background(MaterialTheme.colorScheme.primary)
                else it
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMusicPlaying) Icons.Play(Modifier
            .padding(12.dp)
            .size(24.dp), MaterialTheme.colorScheme.onPrimary)
        else Icons.MusicNote(Modifier
            .padding(12.dp)
            .size(24.dp))
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(0.dp, 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                localMusicViewModel.library[key]?.mediaMetadata?.title?.toString() ?: "!!! null while fetching title !!!",
                modifier = Modifier
                    .padding(8.dp + 16.dp * paddingLeft, 10.dp)
                    .let {
                        if (isMusicPlaying) it.padding(
                            0.dp,
                            (if (index == 0) 0.dp else 2.dp),
                            0.dp,
                            2.dp
                        ) else it
                    }
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 3000,
                        repeatDelayMillis = 2000,
                        velocity = 30.dp
                    )
            )
        }

    }
    if (!isLast) {
        if (!(isMusicPlaying || isNextMusicPlaying)) HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(48.dp, 0.dp, 12.dp, 0.dp)
                .clip(RoundedCornerShape(2.dp))
                .alpha(if (isMusicPlaying || isNextMusicPlaying) 0f else 1f)
        )
    } else {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isMusicPlaying) 0.dp else 2.dp)
        ) {}
    }
}

@Composable
fun FolderListItem(
    name: String,
    isLast: Boolean = false,
    onEnter: () -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(LIST_ITEM_SHAPE)
            .clickable { onEnter() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icons.Folder(Modifier
            .padding(12.dp)
            .size(24.dp))
        Text(
            name,
            modifier = Modifier
                .padding(8.dp, 12.dp)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(48.dp, 0.dp, 12.dp, 0.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    } else {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {}
    }
}


@Composable
fun LocalFolderTabScreen(
    navController: NavHostController,
    playerSheetProxy: PlayerSheetProxy
) {
    val playerViewModel = PlayerViewModel.require()
    val localMusicViewModel = LocalMusicViewModel.require()
//    val navController = navController
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LocalFolderTab.RootFolder,
        enterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(TRANSITION_TWEEN_DURATION)
        ) },
        exitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(TRANSITION_TWEEN_DURATION),
            targetOffset = { it / 4 }
        ) },
        popEnterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(TRANSITION_TWEEN_DURATION),
            initialOffset = { it / 4 }
        ) },
        popExitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(TRANSITION_TWEEN_DURATION)
        ) },
        predictivePopEnterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(TRANSITION_TWEEN_DURATION),
            initialOffset = { it / 4 }
        ) },
        predictivePopExitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(TRANSITION_TWEEN_DURATION)
        ) }
    ) {
        composable<LocalFolderTab.RootFolder> {

            Button(onClick = {
                navController.navigate(LocalFolderTab.Folder("/storage/emulated/0/Music"))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Music Folder")
            }
        }

        composable<LocalFolderTab.Folder> { backStateEntry ->
            val folderArgs: LocalFolderTab.Folder = backStateEntry.toRoute()
            var scannedFolder by remember { mutableStateOf(false) }
            val folders = remember { mutableStateListOf<String>() }
            val musics = remember { mutableStateListOf<String>() }

            if (!scannedFolder) {
                if (folderArgs.folder in localMusicViewModel.cachedFolders.keys) {
                    folders.addAll(localMusicViewModel.cachedFolders[folderArgs.folder]!!.folders)
                    musics.addAll(localMusicViewModel.cachedFolders[folderArgs.folder]!!.musics)
                } else {
                    folderWalker(
                        LocalContext.current,
                        File(folderArgs.folder),
                        folders,
                        musics
                    )
                    folders.sort()
                    musics.sort()
                    localMusicViewModel.cacheFolder(folderArgs.folder, folders, musics)
                }
                scannedFolder = true
            }

            val currentMediaItem = playerViewModel.getCurrentMediaItem()
            var currentLibraryPath by remember { mutableStateOf("") }
            Text(text = currentLibraryPath)

            var currentMedia by remember { mutableStateOf(playerViewModel.getCurrentMediaId()) }
            playerViewModel.onMediaMetadataChanged += { _ ->
                currentMedia = playerViewModel.getCurrentMediaId()
            }

            val keyList = localMusicViewModel.library.keys.sorted()
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                itemsIndexed(folders) { index, path ->
                    FolderListItem(path.split("/").last(), index == folders.lastIndex) {
                        navController.navigate(LocalFolderTab.Folder(path))
                    }
                }
                if (folders.isNotEmpty() && musics.isNotEmpty()) item {
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {}
                }
                itemsIndexed(musics) { index, key ->
                    val isLast = index == musics.lastIndex
                    MusicListItem(
                        key,
                        if (!isLast) musics[index + 1] else null,
                        index,
                        isLast
                    ) {
                        playerViewModel.loadSingleMusic(key)
                        playerViewModel.play()
                    }
                }
                item {
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .fillParentMaxHeight()
                    ) {}
                }
            }
        }
    }
}


@Composable
fun LibraryScreen(
    navController: NavHostController,
    playerSheetProxy: PlayerSheetProxy
) {
//    val context = LocalContext.current
//    val playerViewModel = PlayerViewModel.require()
//    val localMusicViewModel = LocalMusicViewModel.require()
//    val navController = rememberNavController()
//    Text("Main Nav")
//    NavHost(
//        navController = navController,
//        startDestination = LocalFolderTab
//    ) {
//        composable<LocalFolderTab> {
            LocalFolderTabScreen(navController, playerSheetProxy)
//        }
//    }
}
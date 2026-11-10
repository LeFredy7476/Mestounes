package dev.lefredy.mestounes.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import dev.lefredy.mestounes.viewModel.LocalMusicViewModel
import dev.lefredy.mestounes.viewModel.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

val Float.sin01: Float
    get() = cos(this * PI.toFloat()) * -0.5f + 0.5f
val Float.clamp: Float
    get() = max(0.0f, min(1.0f, this))

interface PlayerSheetProxy {
    fun hidePlayer(): Unit {}
    fun showPlayer(): Unit {}
    fun expandPlayer(): Unit {}
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(screenPaddingValues: PaddingValues = PaddingValues(), content : @Composable (PlayerSheetProxy) -> Unit) {

    val playerViewModel = PlayerViewModel.require()

    var isPlaying by remember { mutableStateOf(playerViewModel.isPlaying) }
    playerViewModel.onIsPlayingChanged += fun (playing: Boolean) {
        isPlaying = playing
    }

    val localWindowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val miniPlayerHeight = 80.dp
    val playerPeekHeight = miniPlayerHeight - 8.dp + androidx.compose.ui.unit.max(screenPaddingValues.calculateBottomPadding(), 8.dp)

    val windowSize = localWindowInfo.containerSize
    val windowHeight = windowSize.height.toFloat()
    val slideHeight = max(1.0f,windowHeight - playerPeekHeight.value * density.density - 140)

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            enabledValues = setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded)
        )
    )
    fun sheetAnimationAxis(): Float {
        val rawOffset =
            try { scaffoldState.bottomSheetState.requireOffset() }
            catch (_: IllegalStateException) { slideHeight }
        val ratio = 1.0f * rawOffset / slideHeight
        return max(0.0f, min(1.0f, ratio))
    }

    fun isPlayerOpen(): Boolean {
        return sheetAnimationAxis() < 0.8
    }

    val playerSheetProxy = object: PlayerSheetProxy {}

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = playerPeekHeight,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(
            topStart = miniPlayerHeight * 0.5f * (sheetAnimationAxis() * 4f).clamp,
            topEnd = miniPlayerHeight * 0.5f * (sheetAnimationAxis() * 4f).clamp
        ),
        sheetContainerColor = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer,
            sheetAnimationAxis()
        ),
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {

                BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                }

//                maxHeight
//                maxWidth

                // Mini Player
                MiniPlayer(scope, miniPlayerHeight, scaffoldState, 8.dp, ::sheetAnimationAxis)

                // Full Player
                Box(
                    modifier = Modifier
                        .padding(screenPaddingValues)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .graphicsLayer {
                            alpha = 1f - (sheetAnimationAxis() * 4f - 2f).clamp.sin01
                            val offsetDp =
                                try {
                                    scaffoldState.bottomSheetState.requireOffset()
                                } catch (_: IllegalStateException) {
                                    slideHeight
                                }
                            translationY = -offsetDp
                        }
                ) {
                    var currentMediaItem by remember { mutableStateOf<MediaItem?>(null) }
                    playerViewModel.onMediaMetadataChanged += { metadata ->
                        currentMediaItem = playerViewModel.getCurrentMediaItem()
                    }
                    playerViewModel.onPlayerReady += {
                        currentMediaItem = playerViewModel.getCurrentMediaItem()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            currentMediaItem?.mediaMetadata?.title?.toString() ?: "no song playing",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            currentMediaItem?.mediaMetadata?.artist?.toString() ?: "unknown artist",
                            softWrap = true,
                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = {
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        }) {
                            Text("Collapse Player")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val size = Modifier.size(48.dp)
                            IconButton(
                                onClick = {playerViewModel.prev()},
                                modifier = Modifier.weight(1f),
                                content = { Icons.SkipPrevious(size) }
                            )
                            IconButton(
                                onClick = {
                                    if (isPlaying) playerViewModel.stop()
                                    else playerViewModel.play()
                                },
                                modifier = Modifier.weight(2f),
                                content = { if (isPlaying) Icons.Pause(size) else Icons.Play(size) }
                            )
                            IconButton(
                                onClick = {playerViewModel.skip()},
                                modifier = Modifier.weight(1f),
                                content = { Icons.SkipNext(size) }
                            )
                        }
                        PlaybackSlider(::isPlayerOpen)
                    }
                }
            }
        },
        content = {
            Box(
                modifier = Modifier.padding(
                    start = screenPaddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    end = screenPaddingValues.calculateEndPadding(LocalLayoutDirection.current),
                    top = screenPaddingValues.calculateTopPadding(),
                    bottom = playerPeekHeight - 60.dp
                )
            ) {
                content(playerSheetProxy)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    scope: CoroutineScope,
    miniPlayerHeight: Dp,
    scaffoldState: BottomSheetScaffoldState,
    padding: Dp,
    sheetAnimationAxis: () -> Float
) {

    val iconSize = 40.dp

    val playerViewModel = PlayerViewModel.require()
    val localMusicViewModel = LocalMusicViewModel.require()

    var isPlaying by remember { mutableStateOf(playerViewModel.isPlaying) }
    playerViewModel.onIsPlayingChanged += fun (playing: Boolean) {
        isPlaying = playing
    }

    var currentMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    playerViewModel.onMediaMetadataChanged += { metadata ->
        currentMediaItem = playerViewModel.getCurrentMediaItem()
    }
    playerViewModel.onPlayerReady += {
        currentMediaItem = playerViewModel.getCurrentMediaItem()
    }

    val interactionSource = remember { MutableInteractionSource() }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (0.9f < sheetAnimationAxis()) scope.launch { scaffoldState.bottomSheetState.expand() }
            }
            .height(miniPlayerHeight)
            .padding(padding, padding, miniPlayerHeight / 2 - iconSize / 2, padding)
            .graphicsLayer {
                alpha = (sheetAnimationAxis() * 4f - 3f).clamp.sin01
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(padding)
    ) {
        val artboardShape = RoundedCornerShape(percent = 50)
        Box(
            modifier = Modifier
                .size(miniPlayerHeight - padding * 2)
                .border(
                    2.dp,
                    color = MaterialTheme.colorScheme.inversePrimary,
                    shape = artboardShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icons.MusicNote(Modifier.size(iconSize), MaterialTheme.colorScheme.primary)
        }
        Box(
            modifier = Modifier.weight(1f, fill = true)
        ) {
            Column() {
                Text(
                    currentMediaItem?.mediaMetadata?.title?.toString() ?: "no song playing",
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 3000,
                        repeatDelayMillis = 2000,
                        velocity = 30.dp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    currentMediaItem?.mediaMetadata?.artist?.toString() ?: "unknown artist",
                    softWrap = false,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row() {
            val size = Modifier.size(iconSize)
            IconButton(
                enabled = 0.9f < sheetAnimationAxis(),
                modifier = size,
                onClick = playerViewModel::prev,
                content = { Icons.SkipPrevious(size, MaterialTheme.colorScheme.primary) }
            )
            IconButton(
                enabled = 0.9f < sheetAnimationAxis(),
                modifier = size,
                onClick = { if (isPlaying) playerViewModel.stop() else playerViewModel.play() },
                content = {
                    if (isPlaying) Icons.Pause(size)
                    else Icons.Play(size)
                }
            )
            IconButton(
                enabled = 0.9f < sheetAnimationAxis(),
                modifier = size,
                onClick = playerViewModel::skip,
                content = { Icons.SkipNext(size, MaterialTheme.colorScheme.primary) }
            )
        }
    }
}


fun millisToText(millis: Long): String {
    var s = millis / 1000L
    var m = s / 60L
    s -= (m * 60L)
    val h = m / 60L
    m -= (h * 60L)
    var ts = "$s"
    if (ts.length < 2) ts = "0$ts"
    if (h > 0L) {
        var tm = "$s"
        if (tm.length < 2) tm = "0$tm"
        return "$h:$tm:$ts"
    }
    return "$m:$ts"
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaybackSlider(isPlayerOpen: () -> Boolean) {
    val playerViewModel = PlayerViewModel.require()
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }
    var mediaDuration by remember { mutableLongStateOf(playerViewModel.duration.coerceAtLeast(1L)) }
    var mediaCurrentPosition by remember { mutableLongStateOf(playerViewModel.currentPosition) }

    // Update current position periodically while playing
    LaunchedEffect(playerViewModel) {
        while (true) {
//            Log.d("MainActivity", "effect iteration (${isPlayerOpen()})")
            if (!isSeeking && isPlayerOpen()) {
                currentPosition = playerViewModel.currentPosition.toFloat() / playerViewModel.duration.coerceAtLeast(1L).toFloat()
                mediaCurrentPosition = playerViewModel.currentPosition
                mediaDuration = playerViewModel.duration
            }
            delay(100L.milliseconds) // Update every half second
        }
    }
    Column (
        modifier = Modifier
            .padding(24.dp)
    ) {
        InteractiveWavySlider(
            value = currentPosition,
            onValueChange = { newValue ->
                isSeeking = true
                currentPosition = newValue
                mediaCurrentPosition = (currentPosition * playerViewModel.duration).toLong()
            },
            onValueChangeFinished = {
                playerViewModel.seekTo((currentPosition * playerViewModel.duration).toLong())
                isSeeking = false
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(millisToText(mediaCurrentPosition))
            Text(if (mediaDuration == 0L) "" else millisToText(mediaDuration))
        }
    }

}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InteractiveWavySlider(
    value: Float,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    initialValue: Float = 0.5f,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    onValueChangeCanceled: () -> Unit = {}
) {
    var widthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                widthPx = coordinates.size.width.toFloat()
            }
            .pointerInput(widthPx) {
                if (widthPx <= 0f) return@pointerInput

                detectTapGestures { offset ->
                    val calculated = (offset.x / widthPx).coerceIn(0f, 1f)
                    onValueChange(calculated)
                    onValueChangeFinished()
                }
            }
            .pointerInput(widthPx) {
                if (widthPx <= 0f) return@pointerInput

                detectDragGestures(
                    onDragEnd = { onValueChangeFinished() },
                    onDragCancel = { onValueChangeCanceled() }
                ) { change, _ ->
                    change.consume()
                    val calculated = (change.position.x / widthPx).coerceIn(0f, 1f)
                    onValueChange(calculated)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        LinearWavyProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            wavelength = 24.dp,
            stopSize = 0.dp,
            amplitude = { 1f },
//            color = MaterialTheme.colorScheme.tertiary,
//            trackColor = MaterialTheme.colorScheme.secondary,
            gapSize = (-1).dp,
            waveSpeed = 10.dp
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .offset(x = with(LocalDensity.current) { (value * widthPx).toDp() }, y = 0.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
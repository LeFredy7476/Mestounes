package dev.lefredy.mestounes.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.lefredy.mestounes.core.Track
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.registry.entity.ArtistItem
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import dev.lefredy.mestounes.ui.theme.PremierKotlinAppTheme
import dev.lefredy.mestounes.viewModel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

/**
 * Stateful entry point — connects to LocalLibrary (Room) and PlayerViewModel.
 * Plug this into MainScreen.kt under `MainTab.Tracks`.
 */
@Composable
fun TrackListScreen(
    modifier: Modifier = Modifier,
) {
    val tracks by produceState(initialValue = emptyList<Track>()) {
        val library = LocalLibrary.require()
        library.getTracksAsync().collect { loadedTracks ->
            value = loadedTracks
        }
    }


    val playerViewModel = remember { PlayerViewModel.require() }
    var currentlyPlayingPath by remember { mutableStateOf(playerViewModel.getCurrentMediaId()) }

    LaunchedEffect(playerViewModel) {
        playerViewModel.onPlayerReady += {
            currentlyPlayingPath = playerViewModel.getCurrentMediaId()
        }
        playerViewModel.onMediaMetadataChanged += {
            currentlyPlayingPath = playerViewModel.getCurrentMediaId()
        }
    }

    val scope = rememberCoroutineScope()
    TrackListContent(
        tracks = tracks,
        currentlyPlayingPath = currentlyPlayingPath,
        onTrackClick = { clickedTrack, orderedTracks ->
            scope.launch {
                val startIndex = orderedTracks.indexOf(clickedTrack).coerceAtLeast(0)
                val mediaItems = withContext(Dispatchers.Default) {
                    orderedTracks.map { it.toMediaItem() }
                }
                playerViewModel.loadPlaylist(mediaItems, startIndex)
                playerViewModel.play()
                currentlyPlayingPath = clickedTrack.path
            }
        },
        modifier = modifier,
    )

}

private fun Track.toMediaItem(): MediaItem {
    val uri = if (path.startsWith("file://") || path.startsWith("content://")) {
        Uri.parse(path)
    } else {
        Uri.fromFile(File(path))
    }

    val mediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artists.joinToString(", ") { it.name })
        .setAlbumArtist(albumArtist.name)
        .setAlbumTitle(album)
        .setTrackNumber(trackNumber)
        .setDiscNumber(discNumber)
        .build()

    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(path)
        .setMediaMetadata(mediaMetadata)
        .build()
}

/**
 * Stateless UI — knows nothing about Room or ExoPlayer, making it easy to Preview and test.
 */
@Composable
fun TrackListContent(
    tracks: List<Track>,
    currentlyPlayingPath: String? = null,
    onTrackClick: (clickedTrack: Track, orderedTracks: List<Track>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var selectedSort by remember { mutableStateOf(TrackSortOption.Alphabetical) }
    var sortDirection by remember { mutableStateOf(SortDirection.Ascending) }

    val sections =  remember(tracks, selectedSort, sortDirection) { sortAndGroupTracks(tracks, selectedSort, sortDirection) }

    val orderedTracks = remember(sections) { sections.flatMap { it.tracks } }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        TrackSortBar(
            selectedSort = selectedSort,
            sortDirection = sortDirection,
            onSortSelected = { selectedSort = it },
            onToggleDirection = {
                sortDirection = if (sortDirection == SortDirection.Ascending) {
                    SortDirection.Descending
                } else {
                    SortDirection.Ascending
                }
            },
        )

        if ( tracks.isEmpty() ) { TabPlaceholderView("No tracks yet") { Icons.MusicNote() }}
        else {
            val listState = rememberLazyListState()
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                )
                {
                    sections.forEach {
                        section ->
                        if (section.header != null){
                            item(key = "Header_${section.header}") { SectionHeader(section.header) }
                        }
                        items(section.tracks, key = { it.path }) { track -> TrackListItem(track = track, isPlaying = track.path == currentlyPlayingPath ,onClick = {onTrackClick(track, orderedTracks)} ) }
                    }
                    item ( key = "bottom_spacer" ) { Spacer(Modifier.height(80.dp)) }
                }

                LazyColumnScrollbar(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }



}

@Composable
private fun TrackSortBar(
    selectedSort: TrackSortOption,
    sortDirection: SortDirection,
    onSortSelected: (TrackSortOption) -> Unit,
    onToggleDirection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: horizontally scrollable row of sort buttons
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackSortOption.entries.forEach { option ->
                FilterChip(
                    selected = option == selectedSort,
                    onClick = { onSortSelected(option) },
                    label = { Text(option.label) },
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Right: Asc / Desc toggle button (enabled only for Alphabetical & ReleaseDate)
        TextButton(
            onClick = onToggleDirection,
            enabled = selectedSort.supportsDirection,
        ) {
            Text(
                text = if (sortDirection == SortDirection.Ascending) "↑ Asc" else "↓ Desc"
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
    }
}

private data class ScrollbarMetrics(
    val totalItems: Int,
    val thumbFraction: Float,
    val scrollProgress: Float,
    val isScrollable: Boolean,
)

@Composable
private fun LazyColumnScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val metrics by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleCount = layoutInfo.visibleItemsInfo.size
            if (visibleCount == 0 || totalItems <= visibleCount) {
                ScrollbarMetrics(totalItems, 1f, 0f, isScrollable = false)
            } else {
                val thumbFraction = (visibleCount.toFloat() / totalItems).coerceIn(0.08f, 1f)
                val maxScrollIndex = (totalItems - visibleCount).coerceAtLeast(1)
                val scrollProgress =
                    (listState.firstVisibleItemIndex.toFloat() / maxScrollIndex).coerceIn(0f, 1f)
                ScrollbarMetrics(totalItems, thumbFraction, scrollProgress, isScrollable = true)
            }
        }
    }

    if (!metrics.isScrollable) return

    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(16.dp)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .onGloballyPositioned { coordinates ->
                trackHeightPx = coordinates.size.height.toFloat()
            }
            .pointerInput(metrics.totalItems, trackHeightPx) {
                if (trackHeightPx <= 0f) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                    val targetIndex = (fraction * (metrics.totalItems - 1)).roundToInt()
                    scope.launch { listState.scrollToItem(targetIndex) }
                }
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        if (trackHeightPx > 0f) {
            val thumbHeightPx = trackHeightPx * metrics.thumbFraction
            val thumbOffsetPx = (trackHeightPx - thumbHeightPx) * metrics.scrollProgress
            val thumbHeightDp = with(density) { thumbHeightPx.toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = thumbOffsetPx.roundToInt()) }
                    .width(6.dp)
                    .height(thumbHeightDp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
        }
    }
}

private fun fakeTrack(title: String, artist: String, year: Int?): Track = Track(
    TrackWithArtist(
        track = TrackEntity(
            path = "Music/$artist/$title.mp3",
            trackId = null,
            title = title,
            mediaLength = 210_000L,
            album = "Sample Album",
            albumArtist = artist,
            composer = null,
            date = year,
            trackNumber = 1,
            discNumber = 1,
            comment = "",
        ),
        artists = listOf(ArtistItem(name = artist, index = 0)),
    )
)

@Preview(showBackground = true)
@Composable
fun TrackListPreview() {
    PremierKotlinAppTheme {
//        SectionHeader("test")
//        TrackSortBar(TrackSortOption.Alphabetical,SortDirection.Ascending, onSortSelected = )
        val sampleTracks = listOf(
            fakeTrack("1999", "Prince", 1982),
            fakeTrack("Around the World", "Daft Punk", 1997),
            fakeTrack("aerodynamic", "Daft Punk", 2001),
            fakeTrack("Blinding Lights", "The Weeknd", 2019),
            fakeTrack("Bohemian Rhapsody", "Queen", 1975),
            fakeTrack("Come Together", "The Beatles", 1969),
            fakeTrack("Digital Love", "Daft Punk", 2001),
            fakeTrack("Harder Better Faster Stronger", "Daft Punk", 2001),
            fakeTrack("One More Time", "Daft Punk", 2001),
            fakeTrack("Smells Like Teen Spirit", "Nirvana", 1991),
            fakeTrack("Untitled Demo", "Local Artist", null),
            fakeTrack("Zelda's Lullaby", "Koji Kondo", 1998),
        )
        TrackListContent(
            tracks = sampleTracks,
            currentlyPlayingPath = sampleTracks[1].path,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackSortBarPreview() {
    PremierKotlinAppTheme {
        var selectedSort by remember { mutableStateOf(TrackSortOption.Alphabetical) }
        var sortDirection by remember { mutableStateOf(SortDirection.Ascending) }

        TrackSortBar(
            selectedSort = selectedSort,
            sortDirection = sortDirection,
            onSortSelected = { selectedSort = it },
            onToggleDirection = {
                sortDirection = if (sortDirection == SortDirection.Ascending) {
                    SortDirection.Descending
                } else {
                    SortDirection.Ascending
                }
            },
        )
    }
}
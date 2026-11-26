package dev.lefredy.mestounes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lefredy.mestounes.core.Artist
import dev.lefredy.mestounes.registry.LocalLibrary
import dev.lefredy.mestounes.ui.component.SelectionBar
import dev.lefredy.mestounes.ui.theme.PremierKotlinAppTheme

enum class ArtistSortOrder(val label: String) {
    ASCENDING("A → Z"),
    DESCENDING("Z → A"),
}

@Composable
fun ArtistListView(
    onArtistClick: (Artist) -> Unit = {},
) {
    val context = LocalContext.current
    val artistsFlow = remember(context) {
        LocalLibrary.require(context).getEveryArtistsAsync()
    }
    val artists by artistsFlow.collectAsState(initial = emptyList())

    ArtistListContent(
        artists = artists,
        onArtistClick = onArtistClick,
    )
}

@Composable
fun ArtistListContent(
    artists: List<Artist>,
    initialSelected: Set<Artist> = emptySet(),
    onArtistClick: (Artist) -> Unit = {},
    onPlayArtist: (Artist) -> Unit = {},
    onAddArtistToFavorites: (Artist) -> Unit = {},
) {
    var sortOrder by remember { mutableStateOf(ArtistSortOrder.ASCENDING) }
    var selectedArtists by remember { mutableStateOf(initialSelected) }
    val isSelectionMode = selectedArtists.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
        selectedArtists = emptySet()
    }

    val sortedArtists = remember(artists, sortOrder) {
        when (sortOrder) {
            ArtistSortOrder.ASCENDING -> artists.sortedBy { it.name.lowercase() }
            ArtistSortOrder.DESCENDING -> artists.sortedByDescending { it.name.lowercase() }
        }
    }

    fun toggleSelection(artist: Artist) {
        selectedArtists = if (artist in selectedArtists) {
            selectedArtists - artist
        } else {
            selectedArtists + artist
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = isSelectionMode) {
            SelectionBar(
                selectedCount = selectedArtists.size,
                totalCount = sortedArtists.size,
                onClearSelection = { selectedArtists = emptySet() },
                onSelectAll = { selectedArtists = sortedArtists.toSet() },
            )
        }

        if (!isSelectionMode && sortedArtists.isNotEmpty()) {
            ArtistListSortHeader(
                artistCount = sortedArtists.size,
                sortOrder = sortOrder,
                onToggleSort = {
                    sortOrder = if (sortOrder == ArtistSortOrder.ASCENDING) {
                        ArtistSortOrder.DESCENDING
                    } else {
                        ArtistSortOrder.ASCENDING
                    }
                },
            )
        }

        if (sortedArtists.isEmpty()) {
            ArtistListEmptyState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sortedArtists, key = { it.name }) { artist ->
                    ArtistGridItem(
                        artist = artist,
                        isSelected = artist in selectedArtists,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) toggleSelection(artist)
                            else onArtistClick(artist)
                        },
                        onLongClick = { toggleSelection(artist) },
                        onPlay = { onPlayArtist(artist) },
                        onAddToFavorites = { onAddArtistToFavorites(artist) },
                        onSelect = { toggleSelection(artist) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistListSortHeader(
    artistCount: Int,
    sortOrder: ArtistSortOrder,
    onToggleSort: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$artistCount artists",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onToggleSort) {
            Text(text = "Sort: ${sortOrder.label}")
        }
    }
}

@Composable
private fun ArtistListEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icons.Artist(
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No artists in library yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ArtistGridItem(
    artist: Artist,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPlay: () -> Unit = {},
    onAddToFavorites: () -> Unit = {},
    onSelect: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(12.dp)
    val cardBackground = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardBackground)
            .let {
                if (isSelected) it.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
                else it
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(8.dp),
    ) {
        ArtistCoverThumbnail(isSelected = isSelected)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = artist.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!isSelectionMode) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icons.MoreVert(
                            modifier = Modifier.size(20.dp),
                            description = "Options for ${artist.name}",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play") },
                            onClick = {
                                menuExpanded = false
                                onPlay()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Add to favorites") },
                            onClick = {
                                menuExpanded = false
                                onAddToFavorites()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Select") },
                            onClick = {
                                menuExpanded = false
                                onSelect()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistCoverThumbnail(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icons.Artist(
            modifier = Modifier.size(48.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icons.Check(
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArtistListPreview() {
    val sampleArtists = listOf(
        Artist("Pink Floyd"),
        Artist("Daft Punk"),
        Artist("Radiohead"),
        Artist("Kendrick Lamar"),
        Artist("Miles Davis"),
        Artist("Nujabes"),
    )
    PremierKotlinAppTheme {
        ArtistListContent(artists = sampleArtists)
    }
}

@Preview(showBackground = true)
@Composable
fun ArtistListSelectionPreview() {
    val sampleArtists = listOf(
        Artist("Daft Punk"),
        Artist("Kendrick Lamar"),
        Artist("Pink Floyd"),
        Artist("Radiohead"),
    )
    PremierKotlinAppTheme {
        ArtistListContent(
            artists = sampleArtists,
            initialSelected = setOf(sampleArtists[0], sampleArtists[2]),
        )
    }
}

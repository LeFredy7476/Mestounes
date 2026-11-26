package dev.lefredy.mestounes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lefredy.mestounes.core.Track
import dev.lefredy.mestounes.registry.entity.ArtistItem
import dev.lefredy.mestounes.registry.entity.TrackEntity
import dev.lefredy.mestounes.registry.entity.TrackWithArtist
import dev.lefredy.mestounes.ui.theme.PremierKotlinAppTheme

@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onAddToQueue: () -> Unit = {},
    onShowDetails: () -> Unit = {},
    modifier: Modifier = Modifier,
) {

    var menuExpanded by remember { mutableStateOf(false) }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable{ onClick()}
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying){
                Icons.Play(
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    description = "Play icon"
                )
            }else{
                Icons.MusicNote(
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    description = "Track Cover"
                )
            }
        }
        Spacer(
            Modifier.width(10.dp)
        )
        Column(
            Modifier.weight(1f),
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if(isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artists.joinToString(separator = ", ") { it.name }.ifEmpty { "Unknown Artist" },
                style = MaterialTheme.typography.bodyMedium,
                color = if(isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { menuExpanded = true }) { Icons.MoreVert() }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Add to queue") },
                    onClick = {
                        menuExpanded = false
                        onAddToQueue()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Details") },
                    onClick = {
                        menuExpanded = false
                        onShowDetails()
                    }
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun TrackListItemPreview() {
    PremierKotlinAppTheme {

        val sampleTrack = Track(
            TrackWithArtist(
                track = TrackEntity(
                    path = "Music/Daft Punk/Discovery/01 - One More Time.mp3",
                    trackId = null,
                    title = "One More Time",
                    mediaLength = 320_000L,
                    album = "Discovery",
                    albumArtist = "Daft Punk",
                    composer = null,
                    date = 2001,
                    trackNumber = 1,
                    discNumber = 1,
                    comment = "",
                ),
                artists = listOf(
                    ArtistItem(name = "Daft Punk", index = 0),
                    ArtistItem(name = "Romanthony", index = 1),
                ),
            )
        )

        TrackListItem(track = sampleTrack, onClick = {})

    }
}
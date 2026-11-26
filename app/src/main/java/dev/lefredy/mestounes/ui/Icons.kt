package dev.lefredy.mestounes.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.lefredy.mestounes.R

object Icons {
    @Composable fun Search(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Search") =
        Icon(painterResource(R.drawable.search_24px), description, modifier, tint)
    @Composable fun Play(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Play") =
        Icon(painterResource(R.drawable.play_arrow_24px), description, modifier, tint)
    @Composable fun Pause(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Pause") =
        Icon(painterResource(R.drawable.pause_24px), description, modifier, tint)
    @Composable fun SkipPrevious(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Skip Previous") =
        Icon(painterResource(R.drawable.skip_previous_24px), description, modifier, tint)
    @Composable fun SkipNext(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Skip Next") =
        Icon(painterResource(R.drawable.skip_next_24px), description, modifier, tint)
    @Composable fun Favorite(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Favorite") =
        Icon(painterResource(R.drawable.favorite_24px), description, modifier, tint)
    @Composable fun Stop(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Stop") =
        Icon(painterResource(R.drawable.stop_24px), description, modifier, tint)
    @Composable fun Shuffle(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Shuffle") =
        Icon(painterResource(R.drawable.shuffle_24px), description, modifier, tint)
    @Composable fun MusicNote(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Song") =
        Icon(painterResource(R.drawable.music_note_24px), description, modifier, tint)
    @Composable fun MusicNote2(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Music") =
        Icon(painterResource(R.drawable.music_note_2_24px), description, modifier, tint)
    @Composable fun Folder(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Folder") =
        Icon(painterResource(R.drawable.folder_24px), description, modifier, tint)
    @Composable fun QueueMusic(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Playlists") =
        Icon(painterResource(R.drawable.queue_music_24px), description, modifier, tint)
    @Composable fun Artist(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Artists") =
        Icon(painterResource(R.drawable.artist_24px), description, modifier, tint)
    @Composable fun Album(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Albums") =
        Icon(painterResource(R.drawable.album_24px), description, modifier, tint)
    @Composable fun Equalizer(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Genres") =
        Icon(painterResource(R.drawable.equalizer_24px), description, modifier, tint)
    @Composable fun Tag(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Tags") =
        Icon(painterResource(R.drawable.tag_24px), description, modifier, tint)
    @Composable fun MoreVert(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Menu") =
        Icon(painterResource(R.drawable.more_vert_24px), description, modifier, tint)
    @Composable fun Close(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Close") =
        Icon(painterResource(R.drawable.close_24px), description, modifier, tint)
    @Composable fun Check(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String = "Selected") =
        Icon(painterResource(R.drawable.check_24px), description, modifier, tint)
}
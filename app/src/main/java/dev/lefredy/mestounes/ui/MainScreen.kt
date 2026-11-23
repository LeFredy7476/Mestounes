package dev.lefredy.mestounes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.lefredy.mestounes.R
import dev.lefredy.mestounes.ui.theme.PremierKotlinAppTheme
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    // Discovery("Discovery"), // Skipped for now (Issue #5)
    Playlist("Playlists"),
    Artists("Artists"),
    Albums("Albums"),
    Tracks("Tracks"),
    Genres("Genres"),
    Tags("Tags"),
    Folders("Folders"),
}

@Composable
fun MainScreen(
    navController: NavHostController,
    playerSheetProxy: PlayerSheetProxy,
    isLoggedIn: Boolean = false,
    onAuthClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSearchExecuted: (String) -> Unit = {},
) {
    MainScreenContent(
        isLoggedIn = isLoggedIn,
        onAuthClick = onAuthClick,
        onSettingsClick = onSettingsClick,
        onStatsClick = onStatsClick,
        onSearchExecuted = onSearchExecuted,
    ) { tab ->
        when (tab) {
            MainTab.Playlist -> TabPlaceholderView(tab.title) {
                Icons.QueueMusic(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: Issue #9 - PlaylistList view
            MainTab.Artists -> TabPlaceholderView(tab.title) {
                Icons.Artist(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: Issue #8 - ArtistList view
            MainTab.Albums -> TabPlaceholderView(tab.title) {
                Icons.Album(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: Issue #7 - AlbumList view
            MainTab.Tracks -> TabPlaceholderView(tab.title) {
                Icons.MusicNote(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: Issue #6 - TrackList view
            MainTab.Genres -> TabPlaceholderView(tab.title) {
                Icons.Equalizer(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: Issue #10 - GenreList view
            MainTab.Tags -> TabPlaceholderView(tab.title) {
                Icons.Tag(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            } // TODO: TagList view
            MainTab.Folders -> LocalFolderTabScreen(navController, playerSheetProxy) // TODO: Issue #14 - DirectoryList view
        }
    }
}

@Composable
fun MainScreenContent(
    isLoggedIn: Boolean = false,
    onAuthClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSearchExecuted: (String) -> Unit = {},
    tabContent: @Composable (MainTab) -> Unit,
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val scope = rememberCoroutineScope()

    var isSearchOpen by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        MainTopBar(
            isLoggedIn = isLoggedIn,
            isSearchOpen = isSearchOpen,
            onSearchToggle = { isSearchOpen = !isSearchOpen },
            onAuthClick = onAuthClick,
            onSettingsClick = onSettingsClick,
            onStatsClick = onStatsClick,
            onAboutClick = { showAboutDialog = true },
        )

        AnimatedVisibility(visible = isSearchOpen) {
            SearchInputBar(
                label = "Search library",
                onSearchExecuted = { query ->
                    onSearchExecuted(query)
                }
            )
        }

        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp,
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = tab.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            tabContent(tabs[page])
        }
    }

    if (showAboutDialog) {
        AboutMestounesDialog(onDismiss = { showAboutDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    isLoggedIn: Boolean,
    isSearchOpen: Boolean,
    onSearchToggle: () -> Unit,
    onAuthClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        windowInsets = WindowInsets(0),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icons.MusicNote2(
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        description = "Mestounes Logo"
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                if (isSearchOpen) Icons.Close() else Icons.Search()
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icons.MoreVert()
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isLoggedIn) "Disconnect" else "Log In / Sign In") },
                        onClick = {
                            menuExpanded = false
                            onAuthClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            menuExpanded = false
                            onSettingsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Your Stats") },
                        onClick = {
                            menuExpanded = false
                            onStatsClick()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("About Mestounes") },
                        onClick = {
                            menuExpanded = false
                            onAboutClick()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun TabPlaceholderView(
    title: String,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AboutMestounesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Mestounes") },
        text = {
            Text(
                "Mestounes is a client-first music application focused on local MP3 playback and remote synchronization."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PremierKotlinAppTheme {
        MainScreenContent { tab ->
            TabPlaceholderView(tab.title) {
                Icons.MusicNote(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            }
        }
    }
}

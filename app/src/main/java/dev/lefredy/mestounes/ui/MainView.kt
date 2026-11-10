package dev.lefredy.mestounes.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.lefredy.mestounes.ui.theme.PremierKotlinAppTheme
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import dev.lefredy.mestounes.viewModel.LocalMusicViewModel
import dev.lefredy.mestounes.viewModel.PlayerViewModel

@Composable
fun MainView() {
    val playerViewModel = PlayerViewModel.require()
    val localMusicViewModel = LocalMusicViewModel.require()
    val context = LocalContext.current
    val navController = rememberNavController()

    //rememberManagedExoPlayer()
    PremierKotlinAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            PlayerScreen(screenPaddingValues = innerPadding) { playerSheetProxy ->
                Box {
                    LibraryScreen(navController, playerSheetProxy)
                }
            }
        }
    }
}


@Composable
fun SearchInputBar(onSearchExecuted: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val executeSearch = {
        if (searchQuery.isNotBlank()) {
            onSearchExecuted(searchQuery)
            keyboardController?.hide() // Hide keyboard on search
        }
    }

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        label = {
            Text("path")
        },
//        leadingIcon = {
//            Icon(
//                painter = painterResource(R.drawable.search_24px),
//                contentDescription = "Search Icon"
//            )
//        },
        trailingIcon = {
            // Clickable search button icon on the right side of the text input
            IconButton(onClick = executeSearch) {
                Icons.Search()
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search // Changes the keyboard enter key to a magnifying glass icon
        ),
        keyboardActions = KeyboardActions(
            onSearch = { executeSearch() } // Handles clicks on the keyboard's search button
        ),
        singleLine = true
    )
}

//@Preview(showBackground = true)
//@Composable
//fun MainViewPreview() {
//    val ui = MainView()
//    ui.Compose()
//}
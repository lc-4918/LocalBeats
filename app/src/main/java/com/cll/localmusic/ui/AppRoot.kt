package com.cll.localmusic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Tab("home", "Accueil", Icons.Default.Home)
    data object Folders : Tab("folders", "Dossiers", Icons.Default.Folder)
    data object Playlists : Tab("playlists", "Playlists", Icons.AutoMirrored.Filled.QueueMusic)
    data object Search : Tab("search", "Rechercher", Icons.Default.Search)
}

private val tabs = listOf(Tab.Home, Tab.Folders, Tab.Playlists, Tab.Search)

@Composable
fun MusicAppRoot(vm: MusicViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showChrome = route in tabs.map { it.route } || route?.startsWith("playlist/") == true

    // statusBarsPadding() sur le Box : le contenu s'arrete sous la barre de statut
    // (heure, batterie...) comme avec le Scaffold, mais le bas reste plein ecran
    // pour que le scroll passe sous la NavigationBar transparente.
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Tab.Home.route) { HomeScreen(vm, nav) }
            composable(Tab.Folders.route) { FoldersScreen(vm, nav) }
            composable(Tab.Playlists.route) { PlaylistsScreen(vm, nav) }
            composable(Tab.Search.route) { SearchScreen(vm, nav) }
            composable("playlist/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                PlaylistDetailScreen(vm, nav, id)
            }
            composable("player") { PlayerScreen(vm, nav) }
            composable("settings") { SettingsScreen(vm, nav) }
        }

        // La barre chrome (MiniPlayer opaque + BottomBar transparente) est
        // posee en overlay en bas, par-dessus le NavHost.
        if (showChrome) {
            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                MiniPlayer(vm, onOpen = { nav.navigate("player") })
                BottomBar(nav, route)
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavController, currentRoute: String?) {
    // containerColor avec alpha 2/3 (~0.67) : la barre est semi-transparente,
    // le contenu scrolle visuellement derriere elle.
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.77f),
        modifier = Modifier.navigationBarsPadding()
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

/** Avatar rond en haut a gauche -> parametres. Utilise sur chaque ecran principal. */
@Composable
fun TopAvatarBar(title: String, nav: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { nav.navigate("settings") }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Parametres",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun MiniPlayer(vm: MusicViewModel, onOpen: () -> Unit) {
    val st = vm.controller.state
    if (!st.hasItem) return

    // Pochette resolue de facon asynchrone depuis le folderPath de la piste en cours.
    // Meme pattern que PlayerScreen : produceState se relance a chaque changement de piste.
    val cover by produceState<android.net.Uri?>(initialValue = null, st.folderPath) {
        value = vm.coverFor(st.folderPath)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Pochette : carree 48dp, coins arrondis, fond neutre si pas encore chargee.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    st.title.ifBlank { "Lecture" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    st.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { vm.controller.togglePlayPause() }) {
                Icon(
                    if (st.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Lecture/Pause"
                )
            }
            IconButton(onClick = { vm.controller.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Suivant")
            }
        }
    }
}
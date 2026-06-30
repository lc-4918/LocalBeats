package com.example.localmusic.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.localmusic.AudioFolder
import com.example.localmusic.data.AppSettings
import com.example.localmusic.toTrack

@Composable
fun HomeScreen(vm: MusicViewModel, nav: NavController) {
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())
    val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())
    val recents by vm.recents.collectAsStateWithLifecycle(emptyList())
    val recentTracks = remember(recents) { recents.map { it.toTrack() } }

    Column(Modifier.fillMaxSize()) {
        TopAvatarBar("Accueil", nav)
        if (vm.tracks.isEmpty()) {
            ScanPrompt(vm)
        }
        Text(
            "Dernieres ecoutes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )
        if (recentTracks.isEmpty()) {
            Text(
                "Aucune ecoute recente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            TrackList(
                tracks = recentTracks,
                vm = vm,
                settings = settings,
                playlists = playlists,
                onPlayIndex = { i -> vm.play(recentTracks, i) }
            )
        }
    }
}

@Composable
private fun ScanPrompt(vm: MusicViewModel) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Aucun fichier audio charge. Lance un scan pour indexer le telephone et la carte microSD.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.scan() }, enabled = !vm.isScanning) {
            if (vm.isScanning) {
                CircularProgressIndicator(Modifier.height(18.dp))
                Spacer(Modifier.height(0.dp))
                Text("  Scan en cours...")
            } else {
                Icon(Icons.Default.Refresh, null); Text("  Scanner la musique")
            }
        }
    }
}

@Composable
fun FoldersScreen(vm: MusicViewModel, nav: NavController) {
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())
    val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())
    var openFolder by remember { mutableStateOf<AudioFolder?>(null) }

    // Quand on est dans un sous-dossier, le bouton retour physique/systeme
    // doit remonter d'un dossier (comme la fleche dans l'en-tete) au lieu
    // de quitter l'ecran Dossiers.
    BackHandler(enabled = openFolder != null) {
        openFolder = null
    }

    Column(Modifier.fillMaxSize()) {
        if (openFolder == null) {
            TopAvatarBar("Dossiers", nav)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    "${vm.folders.size} dossier(s)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.scan() }) { Icon(Icons.Default.Refresh, "Rescanner") }
            }
            if (vm.tracks.isEmpty()) ScanPrompt(vm)
            LazyColumn(Modifier.fillMaxWidth()) {
                items(vm.folders, key = { it.path }) { folder ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openFolder = folder }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Folder, null)
                        Spacer(Modifier.height(0.dp))
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${folder.trackCount} titre(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        } else {
            val folder = openFolder!!
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { openFolder = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                }
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = { vm.play(folder.tracks) },
                    enabled = folder.tracks.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.height(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tout lire")
                }
            }
            TrackList(
                tracks = folder.tracks,
                vm = vm,
                settings = settings,
                playlists = playlists,
                onPlayIndex = { i -> vm.play(folder.tracks, i) }
            )
        }
    }
}

@Composable
fun SearchScreen(vm: MusicViewModel, nav: NavController) {
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())
    val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())

    Column(Modifier.fillMaxSize()) {
        TopAvatarBar("Rechercher", nav)
        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Titre, artiste, album, dossier...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )
        if (vm.tracks.isEmpty()) {
            Text(
                "Lance d'abord un scan (Accueil ou Dossiers).",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        val results = vm.searchResults
        if (vm.query.isNotBlank()) {
            Text(
                "${results.size} resultat(s)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }
        TrackList(
            tracks = results,
            vm = vm,
            settings = settings,
            playlists = playlists,
            onPlayIndex = { i -> vm.play(results, i) }
        )
    }
}
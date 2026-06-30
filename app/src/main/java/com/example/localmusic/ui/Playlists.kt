package com.example.localmusic.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.localmusic.data.AppSettings
import com.example.localmusic.toTrack
import androidx.core.net.toUri

@Composable
fun PlaylistsScreen(vm: MusicViewModel, nav: NavController) {
    val playlists by vm.playlists.collectAsStateWithLifecycle(emptyList())
    var showCreate by remember { mutableStateOf(false) }

    if (showCreate) {
        CreatePlaylistDialog(
            onDismiss = { showCreate = false },
            onConfirm = { vm.createPlaylist(it) }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, "Nouvelle playlist")
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            TopAvatarBar("Playlists", nav)
            if (playlists.isEmpty()) {
                Text(
                    "Aucune playlist. Touche + pour en creer une.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                items(playlists, key = { it.id }) { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { nav.navigate("playlist/${p.id}") }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (p.coverUri != null) {
                            AsyncImage(
                                model = p.coverUri.toUri(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.QueueMusic, null)
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${p.trackCount} titre(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(vm: MusicViewModel, nav: NavController, playlistId: Long) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())
    val allPlaylists by vm.playlists.collectAsStateWithLifecycle(emptyList())
    val playlist by vm.playlist(playlistId).collectAsStateWithLifecycle(null)
    val rows by vm.playlistTracks(playlistId).collectAsStateWithLifecycle(emptyList())
    val tracks = remember(rows) { rows.map { it.toTrack() } }

    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* certaines sources ne supportent pas la persistance */ }
            vm.setPlaylistCover(playlistId, uri.toString())
        }
    }

    if (showRename) {
        CreatePlaylistDialog(
            initialName = playlist?.name ?: "",
            onDismiss = { showRename = false },
            onConfirm = { vm.renamePlaylist(playlistId, it) }
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = "Supprimer la playlist",
            message = "Cette action est definitive.",
            confirmLabel = "Supprimer",
            onConfirm = { vm.deletePlaylist(playlistId); nav.popBackStack() },
            onDismiss = { showDelete = false }
        )
    }

    Column(Modifier.fillMaxSize()) {
        // Barre haute
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
            }
            Text(
                playlist?.name ?: "Playlist",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, "Options") }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Renommer") }, onClick = { menuOpen = false; showRename = true })
                    DropdownMenuItem(text = { Text("Changer l'image") }, onClick = { menuOpen = false; coverPicker.launch("image/*") })
                    DropdownMenuItem(text = { Text("Supprimer") }, onClick = { menuOpen = false; showDelete = true })
                }
            }
        }

        TrackList(
            tracks = tracks,
            vm = vm,
            settings = settings,
            playlists = allPlaylists,
            onPlayIndex = { i -> vm.play(tracks, i) },
            allowRemove = true,
            onRemoveSelected = { sel -> vm.removeFromPlaylist(sel.map { it.id }) },
            header = {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (playlist?.coverUri != null) {
                            AsyncImage(
                                model = Uri.parse(playlist!!.coverUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            // Pas d'image : seul le bouton "Ajouter une image" est affiche.
                            // Une fois une image definie, ce bouton disparait ; on peut
                            // toujours la changer via le menu des 3 points en haut.
                            IconButton(onClick = { coverPicker.launch("image/*") }) {
                                Icon(Icons.Default.Image, "Ajouter une image", modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { vm.playShuffled(tracks) }) {
                            Icon(Icons.Default.Shuffle, "Lecture aleatoire", modifier = Modifier.size(24.dp))
                        }
                        IconButton(onClick = {
                            if (tracks.isNotEmpty()) { vm.play(tracks, 0); vm.controller.setRepeatAll() }
                        }) {
                            Icon(Icons.Default.Repeat, "Repeat all", modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { if (tracks.isNotEmpty()) vm.play(tracks, 0) },
                            enabled = tracks.isNotEmpty(),
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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${tracks.size} titre(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        )
    }
}
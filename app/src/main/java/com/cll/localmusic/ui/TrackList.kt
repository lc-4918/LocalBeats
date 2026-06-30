package com.cll.localmusic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cll.localmusic.Track
import com.cll.localmusic.data.AppSettings
import com.cll.localmusic.db.PlaylistWithCount

private fun keyOf(t: Track) = if (t.id != 0L) t.id.toString() else t.uri.toString()

@Composable
fun TrackList(
    tracks: List<Track>,
    vm: MusicViewModel,
    settings: AppSettings,
    playlists: List<PlaylistWithCount>,
    onPlayIndex: (Int) -> Unit,
    allowRemove: Boolean = false,
    onRemoveSelected: (List<Track>) -> Unit = {},
    header: (@Composable () -> Unit)? = null
) {
    val selected = remember { mutableStateMapOf<String, Track>() }
    val selectionMode = selected.isNotEmpty()
    var dialogTracks by remember { mutableStateOf<List<Track>?>(null) }

    if (dialogTracks != null) {
        AddToPlaylistDialog(
            playlists = playlists,
            tracks = dialogTracks!!,
            vm = vm,
            onDismiss = { dialogTracks = null }
        )
    }

    if (selectionMode) {
        SelectionBar(
            count = selected.size,
            allowRemove = allowRemove,
            onClose = { selected.clear() },
            onQueue = { vm.addToQueue(selected.values.toList()); selected.clear() },
            onPlaylist = { dialogTracks = selected.values.toList() },
            onRemove = { onRemoveSelected(selected.values.toList()); selected.clear() }
        )
    }

    LazyColumn(Modifier.fillMaxWidth()) {
        if (header != null) item { header() }
        itemsIndexed(tracks, key = { _, t -> keyOf(t) }) { index, track ->
            val key = keyOf(track)
            TrackRow(
                track = track,
                vm = vm,
                showArt = settings.showAlbumArt,
                showMeta = settings.showMetadata,
                compact = settings.compactRows,
                selectionMode = selectionMode,
                selected = selected.containsKey(key),
                onClick = {
                    if (selectionMode) {
                        if (selected.containsKey(key)) selected.remove(key) else selected[key] = track
                    } else onPlayIndex(index)
                },
                onLongClick = { if (!selected.containsKey(key)) selected[key] = track },
                menuItems = buildList {
                    add("Lire" to { onPlayIndex(index) })
                    add("Ajouter a la file d'attente" to { vm.addToQueue(listOf(track)) })
                    add("Ajouter a une playlist" to { dialogTracks = listOf(track) })
                    if (allowRemove) add("Retirer de la playlist" to { onRemoveSelected(listOf(track)) })
                }
            )
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    allowRemove: Boolean,
    onClose: () -> Unit,
    onQueue: () -> Unit,
    onPlaylist: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Fermer") }
            Text("$count selectionne(s)")
            Spacer(Modifier.width(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onQueue) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, "File d'attente")
                }
                IconButton(onClick = onPlaylist) {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, "Playlist")
                }
                if (allowRemove) {
                    IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, "Retirer") }
                }
            }
        }
    }
}

package com.cll.localmusic.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cll.localmusic.Track
import com.cll.localmusic.db.PlaylistWithCount

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "--:--"
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

/**
 * Pochette = image jpg/png trouvee dans le dossier du fichier.
 * Si aucune image, on n'affiche RIEN (le texte prend toute la place).
 */
@Composable
fun CoverImage(folderPath: String, vm: MusicViewModel, sizeDp: Int) {
    val uri by produceState<Uri?>(initialValue = null, folderPath) {
        value = vm.coverFor(folderPath)
    }
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRow(
    track: Track,
    vm: MusicViewModel,
    showArt: Boolean,
    showMeta: Boolean,
    compact: Boolean,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    menuItems: List<Pair<String, () -> Unit>> = emptyList()
) {
    var menuOpen by remember { mutableStateOf(false) }
    val vPad = if (compact) 6.dp else 10.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = vPad)
    ) {
        if (selectionMode) {
            Checkbox(checked = selected, onCheckedChange = { onClick() })
            Spacer(Modifier.width(4.dp))
        }
        if (showArt) {
            CoverImage(folderPath = track.folderPath, vm = vm, sizeDp = if (compact) 40 else 52)
        }
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showMeta) {
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${track.folderName}  •  ${formatDuration(track.durationMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (menuItems.isNotEmpty()) {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    menuItems.forEach { (label, action) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { menuOpen = false; action() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Nom") }
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()); onDismiss() }
            ) { Text("Creer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** Dialogue : choisir une playlist existante ou en creer une avec les titres. */
@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistWithCount>,
    tracks: List<Track>,
    vm: MusicViewModel,
    onDismiss: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    if (showCreate) {
        CreatePlaylistDialog(
            initialName = tracks.firstOrNull()?.title ?: "",
            onDismiss = { showCreate = false; onDismiss() },
            onConfirm = { vm.createPlaylist(it, tracks) }
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter a une playlist") },
        text = {
            Column {
                DropdownMenuItem(
                    text = { Text("➕  Creer une playlist") },
                    onClick = { showCreate = true }
                )
                playlists.forEach { p ->
                    DropdownMenuItem(
                        text = { Text("${p.name}  (${p.trackCount})") },
                        onClick = { vm.addToPlaylist(p.id, tracks); onDismiss() }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmer",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

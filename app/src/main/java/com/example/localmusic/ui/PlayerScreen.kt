package com.example.localmusic.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun PlayerScreen(vm: MusicViewModel, nav: NavController) {
    val st = vm.controller.state
    val cover by produceState<Uri?>(initialValue = null, st.folderPath) {
        value = vm.coverFor(st.folderPath)
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Default.KeyboardArrowDown, "Reduire")
            }
            Text("Lecture en cours", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        // Pochette en grand (image du dossier si presente, sinon placeholder)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        ) {
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(96.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Metadonnees
        Text(
            st.title.ifBlank { "—" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Text(
            st.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp))

        // Barre de progression
        val dur = st.durationMs.coerceAtLeast(1L)
        Slider(
            value = st.positionMs.coerceIn(0, dur).toFloat(),
            onValueChange = { vm.controller.seekTo(it.toLong()) },
            valueRange = 0f..dur.toFloat()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(st.positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatDuration(st.durationMs), style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(8.dp))

        // Controles
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.controller.toggleShuffle() }) {
                Icon(
                    Icons.Default.Shuffle, "Aleatoire",
                    tint = if (st.shuffle) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { vm.controller.previous() }) {
                Icon(Icons.Default.SkipPrevious, "Precedent", modifier = Modifier.size(40.dp))
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                IconButton(onClick = { vm.controller.togglePlayPause() }) {
                    Icon(
                        if (st.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Lecture/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            IconButton(onClick = { vm.controller.next() }) {
                Icon(Icons.Default.SkipNext, "Suivant", modifier = Modifier.size(40.dp))
            }
            // Repeat : OFF -> ALL -> ONE
            IconButton(onClick = { vm.controller.cycleRepeat() }) {
                val tint = if (st.repeatMode == Player.REPEAT_MODE_OFF)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.primary
                Icon(
                    if (st.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    "Repeter",
                    tint = tint
                )
            }
        }
    }
}

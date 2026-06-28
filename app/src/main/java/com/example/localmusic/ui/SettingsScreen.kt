package com.example.localmusic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.localmusic.data.AppSettings
import com.example.localmusic.data.ThemeMode

@Composable
fun SettingsScreen(vm: MusicViewModel, nav: NavController) {
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
            }
            Text("Parametres", style = MaterialTheme.typography.headlineSmall)
        }

        SectionTitle("1. Scanner la bibliotheque")
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Le scan est toujours manuel. Il indexe le telephone et la carte microSD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.scan() }, enabled = !vm.isScanning) {
                if (vm.isScanning) {
                    CircularProgressIndicator(Modifier.height(18.dp))
                    Text("  Scan en cours...")
                } else {
                    Icon(Icons.Default.Refresh, null); Text("  Scanner maintenant")
                }
            }
            vm.lastScanCount?.let {
                Text("Dernier scan : $it fichier(s).", style = MaterialTheme.typography.bodySmall)
            }
        }

        Divider(Modifier.padding(vertical = 12.dp))

        SectionTitle("2. Personnalisation de l'affichage")
        ToggleRow("Afficher les pochettes (image du dossier)", settings.showAlbumArt) { vm.setShowArt(it) }
        ToggleRow("Lignes compactes", settings.compactRows) { vm.setCompact(it) }

        Divider(Modifier.padding(vertical = 12.dp))

        SectionTitle("3. Mode sombre / clair")
        ThemeOption("Suivre le systeme", settings.themeMode == ThemeMode.SYSTEM) { vm.setTheme(ThemeMode.SYSTEM) }
        ThemeOption("Clair", settings.themeMode == ThemeMode.LIGHT) { vm.setTheme(ThemeMode.LIGHT) }
        ThemeOption("Sombre", settings.themeMode == ThemeMode.DARK) { vm.setTheme(ThemeMode.DARK) }

        Divider(Modifier.padding(vertical = 12.dp))

        SectionTitle("4. Lecture et metadonnees")
        ToggleRow("Afficher artiste / dossier / duree", settings.showMetadata) { vm.setShowMeta(it) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label)
    }
}

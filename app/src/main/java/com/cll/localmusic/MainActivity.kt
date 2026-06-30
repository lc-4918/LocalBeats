package com.cll.localmusic

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cll.localmusic.data.AppSettings
import com.cll.localmusic.data.ThemeMode
import com.cll.localmusic.ui.MusicAppRoot
import com.cll.localmusic.ui.MusicViewModel
import com.cll.localmusic.ui.theme.LocalMusicTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppEntry() }
    }
}

private fun requiredPermissions(): List<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.READ_MEDIA_AUDIO)
        add(Manifest.permission.READ_MEDIA_IMAGES)
        add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AppEntry() {
    val vm: MusicViewModel = viewModel()
    val settings by vm.settings.collectAsStateWithLifecycle(AppSettings())

    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LocalMusicTheme(darkTheme = dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val perms = rememberMultiplePermissionsState(requiredPermissions())
            if (perms.allPermissionsGranted) {
                MusicAppRoot(vm)
            } else {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "L'application a besoin d'acceder a vos fichiers audio (et images de pochette) pour fonctionner.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { perms.launchMultiplePermissionRequest() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Autoriser l'acces")
                    }
                }
            }
        }
    }
}

package com.example.localmusic

import android.net.Uri

/** Un fichier audio scanne depuis le stockage (telephone + carte microSD). */
data class Track(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,           // chemin complet du fichier
    val folderPath: String,     // dossier parent
    val mimeType: String
) {
    val folderName: String get() = folderPath.substringAfterLast('/')
}

/** Un dossier contenant au moins un fichier audio. */
data class AudioFolder(
    val path: String,
    val name: String,
    val trackCount: Int,
    val tracks: List<Track>
)

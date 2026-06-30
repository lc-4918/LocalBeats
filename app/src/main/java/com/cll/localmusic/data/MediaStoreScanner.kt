package com.cll.localmusic.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.cll.localmusic.AudioFolder
import com.cll.localmusic.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lit les fichiers audio via MediaStore. MediaStore indexe a la fois le
 * stockage interne et la carte microSD, donc un seul scan couvre les deux.
 * Le scan est declenche manuellement (voir Parametres / pull-to-refresh).
 */
class MediaStoreScanner(private val context: Context) {

    suspend fun scanTracks(): List<Track> = withContext(Dispatchers.IO) {
        val out = ArrayList<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,        // chemin complet
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_MUSIC
        )
        // Uniquement de la musique (exclut sonneries, notifications, etc.)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val path = c.getString(dataCol) ?: continue
                val folder = path.substringBeforeLast('/', "")
                val uri = ContentUris.withAppendedId(collection, id)
                out.add(
                    Track(
                        id = id,
                        uri = uri,
                        title = c.getString(titleCol) ?: path.substringAfterLast('/'),
                        artist = c.getString(artistCol) ?: "Artiste inconnu",
                        album = c.getString(albumCol) ?: "",
                        durationMs = c.getLong(durCol),
                        path = path,
                        folderPath = folder,
                        mimeType = c.getString(mimeCol) ?: "audio/*"
                    )
                )
            }
        }
        out
    }

    /** Regroupe les titres par dossier (n'expose que les dossiers contenant de l'audio). */
    fun groupIntoFolders(tracks: List<Track>): List<AudioFolder> =
        tracks.groupBy { it.folderPath }
            .map { (path, list) ->
                AudioFolder(
                    path = path,
                    name = path.substringAfterLast('/'),
                    trackCount = list.size,
                    tracks = list.sortedBy { it.title.lowercase() }
                )
            }
            .sortedBy { it.name.lowercase() }

    /**
     * Cherche une image (jpg/png) presente dans le meme dossier qu'un titre,
     * via l'index MediaStore.Images. Renvoie null si aucune.
     */
    suspend fun folderImage(folderPath: String): Uri? = withContext(Dispatchers.IO) {
        if (folderPath.isBlank()) return@withContext null
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val args = arrayOf("$folderPath/%")
        context.contentResolver.query(collection, projection, selection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (c.moveToNext()) {
                val data = c.getString(dataCol) ?: continue
                // L'image doit etre directement dans le dossier (pas un sous-dossier).
                if (data.substringBeforeLast('/') != folderPath) continue
                val lower = data.lowercase()
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                    return@withContext ContentUris.withAppendedId(collection, c.getLong(idCol))
                }
            }
        }
        null
    }
}

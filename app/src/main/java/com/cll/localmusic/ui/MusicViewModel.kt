package com.cll.localmusic.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cll.localmusic.AudioFolder
import com.cll.localmusic.MusicApp
import com.cll.localmusic.Track
import com.cll.localmusic.playback.PlaybackController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicViewModel(app: Application) : AndroidViewModel(app) {

    private val appCtx = app as MusicApp
    private val repo = appCtx.repository
    val settingsStore = appCtx.settings

    val controller = PlaybackController(app.applicationContext)

    // Donnees scannees
    var tracks by mutableStateOf<List<Track>>(emptyList()); private set
    var folders by mutableStateOf<List<AudioFolder>>(emptyList()); private set
    var isScanning by mutableStateOf(false); private set
    var lastScanCount by mutableStateOf<Int?>(null); private set

    // Recherche
    var query by mutableStateOf("")
    val searchResults: List<Track>
        get() = if (query.isBlank()) emptyList() else tracks.filter {
            it.title.contains(query, true) ||
                it.artist.contains(query, true) ||
                it.album.contains(query, true) ||
                it.folderName.contains(query, true)
        }

    // Flows DB
    val recents = repo.recents()
    val playlists = repo.playlists()
    fun playlist(id: Long) = repo.playlist(id)
    fun playlistTracks(id: Long) = repo.playlistTracks(id)
    val settings = settingsStore.settings

    // Cache des pochettes de dossier
    private val coverCache = HashMap<String, Uri?>()

    init {
        controller.connect()
        // Rafraichit la position de lecture pour la barre de progression.
        viewModelScope.launch {
            while (true) {
                if (controller.state.isPlaying) controller.refresh()
                delay(500)
            }
        }
    }

    fun scan() {
        if (isScanning) return
        isScanning = true
        viewModelScope.launch {
            val t = repo.scan()
            tracks = t
            folders = repo.folders(t)
            lastScanCount = t.size
            coverCache.clear()
            isScanning = false
        }
    }

    suspend fun coverFor(folderPath: String): Uri? {
        if (folderPath.isBlank()) return null
        coverCache[folderPath]?.let { return it }
        if (coverCache.containsKey(folderPath)) return null
        val uri = repo.folderImage(folderPath)
        coverCache[folderPath] = uri
        return uri
    }

    // --- Lecture ---
    fun play(tracksToPlay: List<Track>, startIndex: Int = 0) {
        controller.playTracks(tracksToPlay, startIndex)
        tracksToPlay.getOrNull(startIndex)?.let { recordPlay(it) }
    }

    fun playSingle(track: Track) = play(listOf(track), 0)

    fun playShuffled(tracksToPlay: List<Track>) {
        controller.playShuffled(tracksToPlay)
        tracksToPlay.firstOrNull()?.let { recordPlay(it) }
    }

    fun addToQueue(tracksToAdd: List<Track>) = controller.addToQueue(tracksToAdd)

    private fun recordPlay(track: Track) {
        viewModelScope.launch { repo.recordPlay(track) }
    }

    // --- Playlists ---
    fun createPlaylist(name: String, withTracks: List<Track> = emptyList()) {
        viewModelScope.launch {
            if (withTracks.isEmpty()) repo.createPlaylist(name)
            else repo.createPlaylistFrom(name, withTracks)
        }
    }

    fun addToPlaylist(playlistId: Long, tracksToAdd: List<Track>) {
        viewModelScope.launch { repo.addToPlaylist(playlistId, tracksToAdd) }
    }

    fun renamePlaylist(id: Long, name: String) = viewModelScope.launch { repo.renamePlaylist(id, name) }
    fun deletePlaylist(id: Long) = viewModelScope.launch { repo.deletePlaylist(id) }
    fun setPlaylistCover(id: Long, uri: String?) = viewModelScope.launch { repo.setPlaylistCover(id, uri) }
    fun removeFromPlaylist(rowIds: List<Long>) = viewModelScope.launch { repo.removeFromPlaylist(rowIds) }

    // --- Parametres ---
    fun setTheme(mode: com.cll.localmusic.data.ThemeMode) =
        viewModelScope.launch { settingsStore.setTheme(mode) }
    fun setShowArt(v: Boolean) = viewModelScope.launch { settingsStore.setShowArt(v) }
    fun setShowMeta(v: Boolean) = viewModelScope.launch { settingsStore.setShowMeta(v) }
    fun setCompact(v: Boolean) = viewModelScope.launch { settingsStore.setCompact(v) }

    override fun onCleared() {
        controller.release()
        super.onCleared()
    }
}

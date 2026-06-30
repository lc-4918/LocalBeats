package com.cll.localmusic.playback

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cll.localmusic.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

data class PlayerUiState(
    val hasItem: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val uri: String = "",
    val folderPath: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffle: Boolean = false
)

fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
    .setUri(uri)
    .setMediaId(uri.toString())
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            // On range le dossier dans une extra pour retrouver la pochette.
            .setExtras(android.os.Bundle().apply { putString("folderPath", folderPath) })
            .build()
    )
    .build()

/**
 * Connecte un MediaController au PlaybackService et expose l'etat de lecture
 * sous forme d'etat Compose observable.
 */
class PlaybackController(private val context: Context) {

    private var controller: MediaController? = null
    private var future: ListenableFuture<MediaController>? = null

    var state by mutableStateOf(PlayerUiState())
        private set

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            refresh()
        }
    }

    fun connect() {
        if (controller != null || future != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        future = MediaController.Builder(context, token).buildAsync().also { f ->
            f.addListener({
                controller = f.get()
                controller?.addListener(listener)
                refresh()
            }, MoreExecutors.directExecutor())
        }
    }

    fun release() {
        controller?.removeListener(listener)
        future?.let { MediaController.releaseFuture(it) }
        controller = null
        future = null
    }

    /** A appeler regulierement pour la barre de progression. */
    fun refresh() {
        val c = controller ?: return
        val item = c.currentMediaItem
        val md = item?.mediaMetadata
        state = PlayerUiState(
            hasItem = item != null,
            isPlaying = c.isPlaying,
            title = md?.title?.toString() ?: "",
            artist = md?.artist?.toString() ?: "",
            uri = item?.localConfiguration?.uri?.toString() ?: item?.mediaId ?: "",
            folderPath = md?.extras?.getString("folderPath") ?: "",
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.let { if (it > 0) it else 0L },
            repeatMode = c.repeatMode,
            shuffle = c.shuffleModeEnabled
        )
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        c.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        c.prepare()
        c.play()
        refresh()
    }

    fun addToQueue(tracks: List<Track>) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        if (c.mediaItemCount == 0) {
            playTracks(tracks)
        } else {
            c.addMediaItems(tracks.map { it.toMediaItem() })
        }
        refresh()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
        refresh()
    }

    fun next() { controller?.seekToNextMediaItem(); refresh() }
    fun previous() { controller?.seekToPreviousMediaItem(); refresh() }
    fun seekTo(ms: Long) { controller?.seekTo(ms); refresh() }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        refresh()
    }

    fun setRepeatAll() { controller?.repeatMode = Player.REPEAT_MODE_ALL; refresh() }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
        refresh()
    }

    fun playShuffled(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val shuffled = tracks.shuffled()
        playTracks(shuffled, 0)
        controller?.shuffleModeEnabled = true
        refresh()
    }
}

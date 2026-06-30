package com.cll.localmusic

import com.cll.localmusic.data.MediaStoreScanner
import com.cll.localmusic.db.MusicDao
import com.cll.localmusic.db.PlaylistEntity
import com.cll.localmusic.db.PlaylistTrackEntity
import com.cll.localmusic.db.RecentPlayEntity

/** Convertit un Track scanne en ligne de playlist persistee. */
fun Track.toPlaylistTrack(playlistId: Long, position: Int) = PlaylistTrackEntity(
    playlistId = playlistId,
    trackUri = uri.toString(),
    title = title, artist = artist, album = album,
    durationMs = durationMs, path = path, folderPath = folderPath,
    mimeType = mimeType, position = position
)

/** Convertit une ligne de playlist persistee en Track lisible. */
fun PlaylistTrackEntity.toTrack() = Track(
    id = id,
    uri = android.net.Uri.parse(trackUri),
    title = title, artist = artist, album = album,
    durationMs = durationMs, path = path, folderPath = folderPath, mimeType = mimeType
)

fun RecentPlayEntity.toTrack() = Track(
    id = 0,
    uri = android.net.Uri.parse(trackUri),
    title = title, artist = artist, album = album,
    durationMs = durationMs, path = path, folderPath = folderPath, mimeType = mimeType
)

class MusicRepository(
    private val scanner: MediaStoreScanner,
    val dao: MusicDao
) {
    suspend fun scan(): List<Track> = scanner.scanTracks()
    fun folders(tracks: List<Track>) = scanner.groupIntoFolders(tracks)
    suspend fun folderImage(folderPath: String) = scanner.folderImage(folderPath)

    // Playlists
    fun playlists() = dao.playlistsWithCount()
    fun playlist(id: Long) = dao.playlist(id)
    fun playlistTracks(id: Long) = dao.playlistTracks(id)

    suspend fun createPlaylist(name: String): Long =
        dao.createPlaylist(PlaylistEntity(name = name))

    suspend fun createPlaylistFrom(name: String, tracks: List<Track>): Long {
        val id = createPlaylist(name)
        addToPlaylist(id, tracks)
        return id
    }

    suspend fun addToPlaylist(playlistId: Long, tracks: List<Track>) {
        val rows = tracks.map { it.toPlaylistTrack(playlistId, 0) }
        dao.addTracks(playlistId, rows)
    }

    suspend fun renamePlaylist(id: Long, name: String) = dao.renamePlaylist(id, name)
    suspend fun setPlaylistCover(id: Long, uri: String?) = dao.setPlaylistCover(id, uri)
    suspend fun deletePlaylist(id: Long) = dao.deletePlaylist(id)
    suspend fun removeFromPlaylist(rowIds: List<Long>) = dao.removeTracks(rowIds)

    // Historique
    fun recents() = dao.recentPlays()
    suspend fun recordPlay(track: Track) {
        dao.recordPlay(
            RecentPlayEntity(
                trackUri = track.uri.toString(),
                title = track.title, artist = track.artist, album = track.album,
                durationMs = track.durationMs, path = track.path, folderPath = track.folderPath,
                mimeType = track.mimeType, playedAt = System.currentTimeMillis()
            )
        )
    }
}

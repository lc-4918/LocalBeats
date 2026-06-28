package com.example.localmusic.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ---------- Entites ----------

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playlistId")]
)
data class PlaylistTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val trackUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val folderPath: String,
    val mimeType: String,
    val position: Int
)

@Entity(tableName = "recent_plays")
data class RecentPlayEntity(
    @PrimaryKey val trackUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val folderPath: String,
    val mimeType: String,
    val playedAt: Long
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val coverUri: String?,
    val createdAt: Long,
    val trackCount: Int
)

// ---------- DAO ----------

@Dao
interface MusicDao {

    // Playlists
    @Insert
    suspend fun createPlaylist(p: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(p: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun renamePlaylist(id: Long, name: String)

    @Query("UPDATE playlists SET coverUri = :uri WHERE id = :id")
    suspend fun setPlaylistCover(id: Long, uri: String?)

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun playlist(id: Long): Flow<PlaylistEntity?>

    @Query(
        """
        SELECT p.id, p.name, p.coverUri, p.createdAt,
               (SELECT COUNT(*) FROM playlist_tracks t WHERE t.playlistId = p.id) AS trackCount
        FROM playlists p ORDER BY p.createdAt DESC
        """
    )
    fun playlistsWithCount(): Flow<List<PlaylistWithCount>>

    // Titres d'une playlist
    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    fun playlistTracks(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Insert
    suspend fun insertTrack(t: PlaylistTrackEntity)

    @Transaction
    suspend fun addTracks(playlistId: Long, tracks: List<PlaylistTrackEntity>) {
        var pos = maxPosition(playlistId) + 1
        for (t in tracks) {
            insertTrack(t.copy(playlistId = playlistId, position = pos))
            pos++
        }
    }

    @Query("DELETE FROM playlist_tracks WHERE id IN (:ids)")
    suspend fun removeTracks(ids: List<Long>)

    // Historique (accueil : dernieres ecoutes)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPlay(r: RecentPlayEntity)

    @Query("SELECT * FROM recent_plays ORDER BY playedAt DESC LIMIT :limit")
    fun recentPlays(limit: Int = 30): Flow<List<RecentPlayEntity>>

    @Query("DELETE FROM recent_plays")
    suspend fun clearRecents()
}

// ---------- Database ----------

@Database(
    entities = [PlaylistEntity::class, PlaylistTrackEntity::class, RecentPlayEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun dao(): MusicDao
}

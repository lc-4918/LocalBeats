package com.cll.localmusic

import android.app.Application
import androidx.room.Room
import com.cll.localmusic.data.MediaStoreScanner
import com.cll.localmusic.data.SettingsStore
import com.cll.localmusic.db.MusicDatabase

/**
 * Service locator minimaliste : evite Hilt pour reduire la surface d'erreur.
 * Les singletons sont accessibles via (application as MusicApp).
 */
class MusicApp : Application() {

    val database: MusicDatabase by lazy {
        Room.databaseBuilder(this, MusicDatabase::class.java, "music.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val scanner: MediaStoreScanner by lazy { MediaStoreScanner(this) }
    val settings: SettingsStore by lazy { SettingsStore(this) }
    val repository: MusicRepository by lazy { MusicRepository(scanner, database.dao()) }
}

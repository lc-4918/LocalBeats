package com.example.localmusic.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** 0 = systeme, 1 = clair, 2 = sombre */
enum class ThemeMode(val value: Int) { SYSTEM(0), LIGHT(1), DARK(2) }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showAlbumArt: Boolean = true,
    val showMetadata: Boolean = true,
    val compactRows: Boolean = false
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val SHOW_ART = booleanPreferencesKey("show_art")
        val SHOW_META = booleanPreferencesKey("show_meta")
        val COMPACT = booleanPreferencesKey("compact_rows")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.value == (p[Keys.THEME] ?: 0) } ?: ThemeMode.SYSTEM,
            showAlbumArt = p[Keys.SHOW_ART] ?: true,
            showMetadata = p[Keys.SHOW_META] ?: true,
            compactRows = p[Keys.COMPACT] ?: false
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.value }
    suspend fun setShowArt(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_ART] = v }
    suspend fun setShowMeta(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_META] = v }
    suspend fun setCompact(v: Boolean) = context.dataStore.edit { it[Keys.COMPACT] = v }
}

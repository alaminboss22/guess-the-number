package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "guess_number_settings")

data class UserPreferences(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val selectedDifficulty: GameDifficulty = GameDifficulty.MEDIUM
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val DIFFICULTY = stringPreferencesKey("difficulty")
    }

    val preferencesFlow: Flow<UserPreferences> = context.settingsDataStore.data.map { prefs ->
        val languageName = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.name
        val difficultyName = prefs[Keys.DIFFICULTY] ?: GameDifficulty.MEDIUM.name
        UserPreferences(
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            darkMode = prefs[Keys.DARK_MODE] ?: true,
            language = runCatching { AppLanguage.valueOf(languageName) }.getOrDefault(AppLanguage.ENGLISH),
            selectedDifficulty = runCatching { GameDifficulty.valueOf(difficultyName) }.getOrDefault(GameDifficulty.MEDIUM)
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setSelectedDifficulty(difficulty: GameDifficulty) {
        context.settingsDataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }
}

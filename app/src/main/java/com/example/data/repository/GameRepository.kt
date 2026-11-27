package com.example.data.repository

import com.example.data.local.GameRecordDao
import com.example.data.local.GameRecordEntity
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserPreferences
import com.example.model.AppLanguage
import com.example.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

fun interface NumberGenerator {
    fun nextSecretNumber(): Int
}

class DefaultNumberGenerator : NumberGenerator {
    override fun nextSecretNumber(): Int = Random.nextInt(1, 101)
}

interface GameRepository {
    val userPreferences: Flow<UserPreferences>
    val gameHistory: Flow<List<GameRecordEntity>>
    val bestScore: Flow<Int>
    val totalGames: Flow<Int>
    val winsCount: Flow<Int>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setSelectedDifficulty(difficulty: GameDifficulty)
    suspend fun saveGameRecord(record: GameRecordEntity)
    suspend fun resetAllStatistics()
}

class OfflineGameRepository(
    private val dao: GameRecordDao,
    private val settingsDataStore: SettingsDataStore
) : GameRepository {
    override val userPreferences: Flow<UserPreferences> = settingsDataStore.preferencesFlow
    override val gameHistory: Flow<List<GameRecordEntity>> = dao.getAllRecords()
    override val bestScore: Flow<Int> = dao.getBestScore()
    override val totalGames: Flow<Int> = dao.getTotalGames()
    override val winsCount: Flow<Int> = dao.getWinsCount()

    override suspend fun setSoundEnabled(enabled: Boolean) = settingsDataStore.setSoundEnabled(enabled)
    override suspend fun setVibrationEnabled(enabled: Boolean) = settingsDataStore.setVibrationEnabled(enabled)
    override suspend fun setMusicEnabled(enabled: Boolean) = settingsDataStore.setMusicEnabled(enabled)
    override suspend fun setDarkMode(enabled: Boolean) = settingsDataStore.setDarkMode(enabled)
    override suspend fun setLanguage(language: AppLanguage) = settingsDataStore.setLanguage(language)
    override suspend fun setSelectedDifficulty(difficulty: GameDifficulty) =
        settingsDataStore.setSelectedDifficulty(difficulty)

    override suspend fun saveGameRecord(record: GameRecordEntity) = dao.insertRecord(record)
    override suspend fun resetAllStatistics() = dao.clearAllRecords()
}

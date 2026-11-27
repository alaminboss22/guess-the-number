package com.example.model

enum class GameDifficulty(
    val maxAttempts: Int,
    val scoreMultiplier: Int,
    val basePoints: Int
) {
    EASY(maxAttempts = 15, scoreMultiplier = 1, basePoints = 500),
    MEDIUM(maxAttempts = 10, scoreMultiplier = 2, basePoints = 800),
    HARD(maxAttempts = 5, scoreMultiplier = 3, basePoints = 1200)
}

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    SPANISH("Español", "es"),
    FRENCH("Français", "fr"),
    BENGALI("বাংলা", "bn"),
    HINDI("हिन्दी", "hi")
}

enum class GuessFeedback {
    NONE,
    TOO_HIGH,
    TOO_LOW,
    CORRECT,
    INVALID
}

data class GuessEntry(
    val value: Int,
    val feedback: GuessFeedback
)

data class GameResult(
    val isWin: Boolean,
    val secretNumber: Int,
    val attemptsUsed: Int,
    val maxAttempts: Int,
    val score: Int,
    val bestScore: Int,
    val isNewRecord: Boolean,
    val difficulty: GameDifficulty
)

data class GameUiState(
    val difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val secretNumber: Int = 50,
    val currentInput: String = "",
    val attemptsRemaining: Int = GameDifficulty.MEDIUM.maxAttempts,
    val maxAttempts: Int = GameDifficulty.MEDIUM.maxAttempts,
    val currentScore: Int = GameDifficulty.MEDIUM.basePoints,
    val feedback: GuessFeedback = GuessFeedback.NONE,
    val lastGuess: Int? = null,
    val guesses: List<GuessEntry> = emptyList(),
    val minPossible: Int = 1,
    val maxPossible: Int = 100,
    val activeHintText: String? = null,
    val hintsUsed: Int = 0,
    val maxHints: Int = 2,
    val isGameOver: Boolean = false,
    val lastResult: GameResult? = null,
    val inputError: Boolean = false
) {
    val attemptsUsed: Int get() = maxAttempts - attemptsRemaining
    val progressFraction: Float
        get() = if (maxAttempts <= 0) 0f else attemptsRemaining.toFloat() / maxAttempts.toFloat()
}

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val selectedDifficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val bestScore: Int = 0,
    val totalGames: Int = 0,
    val winsCount: Int = 0
)

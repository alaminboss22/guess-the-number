package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.BackgroundMusicController
import com.example.audio.HapticController
import com.example.audio.SoundEffectsController
import com.example.data.local.GameRecordEntity
import com.example.data.repository.DefaultNumberGenerator
import com.example.data.repository.GameRepository
import com.example.data.repository.NumberGenerator
import com.example.localization.AppStrings
import com.example.model.AppLanguage
import com.example.model.GameDifficulty
import com.example.model.GameResult
import com.example.model.GameUiState
import com.example.model.GuessEntry
import com.example.model.GuessFeedback
import com.example.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HintType {
    EVEN,
    ODD,
    DIVISIBLE_BY_5,
    PRIME,
    NARROW_RANGE
}

data class ActiveHint(
    val type: HintType,
    val rangeLow: Int = 1,
    val rangeHigh: Int = 100
)

class GameViewModel(
    private val repository: GameRepository,
    private val numberGenerator: NumberGenerator = DefaultNumberGenerator(),
    private val soundEffects: SoundEffectsController,
    private val musicController: BackgroundMusicController,
    private val haptics: HapticController
) : ViewModel() {

    val settingsState: StateFlow<SettingsUiState> = combine(
        repository.userPreferences,
        repository.bestScore,
        repository.totalGames,
        repository.winsCount
    ) { prefs, best, total, wins ->
        SettingsUiState(
            soundEnabled = prefs.soundEnabled,
            vibrationEnabled = prefs.vibrationEnabled,
            musicEnabled = prefs.musicEnabled,
            darkMode = prefs.darkMode,
            language = prefs.language,
            selectedDifficulty = prefs.selectedDifficulty,
            bestScore = best,
            totalGames = total,
            winsCount = wins
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )

    val gameHistory: StateFlow<List<GameRecordEntity>> = repository.gameHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _gameState = MutableStateFlow(createInitialGameState(GameDifficulty.MEDIUM))
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private val _activeHint = MutableStateFlow<ActiveHint?>(null)
    val activeHint: StateFlow<ActiveHint?> = _activeHint.asStateFlow()

    private val _duplicateGuessFlag = MutableStateFlow(false)
    val duplicateGuessFlag: StateFlow<Boolean> = _duplicateGuessFlag.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferences.collect { prefs ->
                musicController.setEnabled(prefs.musicEnabled)
            }
        }
    }

    private fun createInitialGameState(difficulty: GameDifficulty): GameUiState {
        val secret = numberGenerator.nextSecretNumber().coerceIn(1, 100)
        return GameUiState(
            difficulty = difficulty,
            secretNumber = secret,
            currentInput = "",
            attemptsRemaining = difficulty.maxAttempts,
            maxAttempts = difficulty.maxAttempts,
            currentScore = difficulty.basePoints,
            feedback = GuessFeedback.NONE,
            lastGuess = null,
            guesses = emptyList(),
            minPossible = 1,
            maxPossible = 100,
            activeHintText = null,
            hintsUsed = 0,
            isGameOver = false,
            lastResult = null,
            inputError = false
        )
    }

    fun startNewGame(difficulty: GameDifficulty = settingsState.value.selectedDifficulty) {
        playTapFeedback()
        _activeHint.value = null
        _duplicateGuessFlag.value = false
        _gameState.value = createInitialGameState(difficulty)
    }

    fun restartGame() {
        startNewGame(_gameState.value.difficulty)
    }

    fun appendDigit(digit: Int) {
        if (_gameState.value.isGameOver) return
        if (digit !in 0..9) return
        playTapFeedback()
        _duplicateGuessFlag.value = false
        _gameState.update { state ->
            val current = state.currentInput
            if (current.isEmpty() && digit == 0) {
                return@update state
            }
            val candidate = current + digit.toString()
            val parsed = candidate.toIntOrNull()
            if (candidate.length <= 3 && parsed != null && parsed <= 100) {
                state.copy(currentInput = candidate, inputError = false)
            } else {
                state
            }
        }
    }

    fun backspaceInput() {
        if (_gameState.value.isGameOver) return
        playTapFeedback()
        _duplicateGuessFlag.value = false
        _gameState.update { state ->
            if (state.currentInput.isEmpty()) state
            else state.copy(currentInput = state.currentInput.dropLast(1), inputError = false)
        }
    }

    fun clearInput() {
        if (_gameState.value.isGameOver) return
        playTapFeedback()
        _duplicateGuessFlag.value = false
        _gameState.update { state ->
            state.copy(currentInput = "", inputError = false)
        }
    }

    fun updateDirectInput(raw: String) {
        if (_gameState.value.isGameOver) return
        val filtered = raw.filter { it.isDigit() }.take(3)
        _duplicateGuessFlag.value = false
        _gameState.update { it.copy(currentInput = filtered, inputError = false) }
    }

    fun submitGuess() {
        val state = _gameState.value
        if (state.isGameOver) return

        val guess = state.currentInput.toIntOrNull()
        if (guess == null || guess !in 1..100) {
            _duplicateGuessFlag.value = false
            _gameState.update {
                it.copy(feedback = GuessFeedback.INVALID, inputError = true)
            }
            if (settingsState.value.soundEnabled) soundEffects.playError()
            if (settingsState.value.vibrationEnabled) haptics.tap()
            return
        }

        if (state.guesses.any { it.value == guess }) {
            _duplicateGuessFlag.value = true
            _gameState.update {
                it.copy(feedback = GuessFeedback.INVALID, inputError = true, currentInput = "")
            }
            if (settingsState.value.soundEnabled) soundEffects.playError()
            if (settingsState.value.vibrationEnabled) haptics.tap()
            return
        }

        _duplicateGuessFlag.value = false
        val newAttemptsRemaining = state.attemptsRemaining - 1
        val attemptsUsed = state.maxAttempts - newAttemptsRemaining

        if (guess == state.secretNumber) {
            val finalScore = calculateWinScore(
                difficulty = state.difficulty,
                attemptsRemainingAfterWin = newAttemptsRemaining,
                hintsUsed = state.hintsUsed
            )
            val previousBest = settingsState.value.bestScore
            val isNewRecord = finalScore > previousBest
            val newBest = maxOf(finalScore, previousBest)

            val updatedGuesses = state.guesses + GuessEntry(guess, GuessFeedback.CORRECT)
            val result = GameResult(
                isWin = true,
                secretNumber = state.secretNumber,
                attemptsUsed = attemptsUsed,
                maxAttempts = state.maxAttempts,
                score = finalScore,
                bestScore = newBest,
                isNewRecord = isNewRecord,
                difficulty = state.difficulty
            )

            _gameState.update {
                it.copy(
                    currentInput = "",
                    attemptsRemaining = newAttemptsRemaining,
                    currentScore = finalScore,
                    feedback = GuessFeedback.CORRECT,
                    lastGuess = guess,
                    guesses = updatedGuesses,
                    isGameOver = true,
                    lastResult = result,
                    inputError = false
                )
            }

            if (settingsState.value.soundEnabled) soundEffects.playWin()
            if (settingsState.value.vibrationEnabled) haptics.win()
            persistRecord(
                difficulty = state.difficulty,
                secret = state.secretNumber,
                attemptsUsed = attemptsUsed,
                maxAttempts = state.maxAttempts,
                score = finalScore,
                isWin = true,
                guesses = updatedGuesses
            )
            return
        }

        val directionFeedback = if (guess > state.secretNumber) {
            GuessFeedback.TOO_HIGH
        } else {
            GuessFeedback.TOO_LOW
        }

        val updatedMin = if (directionFeedback == GuessFeedback.TOO_LOW) {
            maxOf(state.minPossible, guess + 1)
        } else {
            state.minPossible
        }
        val updatedMax = if (directionFeedback == GuessFeedback.TOO_HIGH) {
            minOf(state.maxPossible, guess - 1)
        } else {
            state.maxPossible
        }

        val updatedGuesses = state.guesses + GuessEntry(guess, directionFeedback)

        if (newAttemptsRemaining <= 0) {
            val result = GameResult(
                isWin = false,
                secretNumber = state.secretNumber,
                attemptsUsed = state.maxAttempts,
                maxAttempts = state.maxAttempts,
                score = 0,
                bestScore = settingsState.value.bestScore,
                isNewRecord = false,
                difficulty = state.difficulty
            )

            _gameState.update {
                it.copy(
                    currentInput = "",
                    attemptsRemaining = 0,
                    currentScore = 0,
                    feedback = directionFeedback,
                    lastGuess = guess,
                    guesses = updatedGuesses,
                    minPossible = updatedMin,
                    maxPossible = updatedMax,
                    isGameOver = true,
                    lastResult = result,
                    inputError = false
                )
            }

            if (settingsState.value.soundEnabled) soundEffects.playLose()
            if (settingsState.value.vibrationEnabled) haptics.lose()
            persistRecord(
                difficulty = state.difficulty,
                secret = state.secretNumber,
                attemptsUsed = state.maxAttempts,
                maxAttempts = state.maxAttempts,
                score = 0,
                isWin = false,
                guesses = updatedGuesses
            )
        } else {
            val previewScore = calculateWinScore(
                difficulty = state.difficulty,
                attemptsRemainingAfterWin = newAttemptsRemaining - 1,
                hintsUsed = state.hintsUsed
            )
            _gameState.update {
                it.copy(
                    currentInput = "",
                    attemptsRemaining = newAttemptsRemaining,
                    currentScore = previewScore,
                    feedback = directionFeedback,
                    lastGuess = guess,
                    guesses = updatedGuesses,
                    minPossible = updatedMin,
                    maxPossible = updatedMax,
                    inputError = false
                )
            }
            if (settingsState.value.soundEnabled) {
                if (directionFeedback == GuessFeedback.TOO_HIGH) soundEffects.playTooHigh()
                else soundEffects.playTooLow()
            }
            if (settingsState.value.vibrationEnabled) haptics.tap()
        }
    }

    fun requestHint() {
        val state = _gameState.value
        if (state.isGameOver || state.hintsUsed >= state.maxHints) return

        val secret = state.secretNumber
        val hint = if (state.hintsUsed == 0) {
            when {
                secret % 5 == 0 -> ActiveHint(HintType.DIVISIBLE_BY_5)
                isPrime(secret) -> ActiveHint(HintType.PRIME)
                secret % 2 == 0 -> ActiveHint(HintType.EVEN)
                else -> ActiveHint(HintType.ODD)
            }
        } else {
            val span = (state.maxPossible - state.minPossible).coerceAtLeast(4)
            val halfWindow = (span / 4).coerceIn(3, 12)
            val low = (secret - halfWindow).coerceAtLeast(state.minPossible)
            val high = (secret + halfWindow).coerceAtMost(state.maxPossible)
            ActiveHint(HintType.NARROW_RANGE, rangeLow = low, rangeHigh = high)
        }

        _activeHint.value = hint
        val newHintsUsed = state.hintsUsed + 1
        val updatedScore = calculateWinScore(
            difficulty = state.difficulty,
            attemptsRemainingAfterWin = (state.attemptsRemaining - 1).coerceAtLeast(0),
            hintsUsed = newHintsUsed
        )

        _gameState.update {
            it.copy(
                hintsUsed = newHintsUsed,
                currentScore = updatedScore
            )
        }
        if (settingsState.value.soundEnabled) soundEffects.playHint()
        if (settingsState.value.vibrationEnabled) haptics.hint()
    }

    fun resolveHintText(hint: ActiveHint?, strings: AppStrings): String? {
        if (hint == null) return null
        return when (hint.type) {
            HintType.EVEN -> strings.hintEven
            HintType.ODD -> strings.hintOdd
            HintType.DIVISIBLE_BY_5 -> strings.hintDivisible5
            HintType.PRIME -> strings.hintPrime
            HintType.NARROW_RANGE -> strings.hintRange(hint.rangeLow, hint.rangeHigh)
        }
    }

    private fun calculateWinScore(
        difficulty: GameDifficulty,
        attemptsRemainingAfterWin: Int,
        hintsUsed: Int
    ): Int {
        val bonus = attemptsRemainingAfterWin.coerceAtLeast(0) * 120 * difficulty.scoreMultiplier
        val penalty = hintsUsed * 60
        return (difficulty.basePoints + bonus - penalty).coerceAtLeast(100)
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }

    private fun persistRecord(
        difficulty: GameDifficulty,
        secret: Int,
        attemptsUsed: Int,
        maxAttempts: Int,
        score: Int,
        isWin: Boolean,
        guesses: List<GuessEntry>
    ) {
        viewModelScope.launch {
            repository.saveGameRecord(
                GameRecordEntity(
                    difficultyName = difficulty.name,
                    secretNumber = secret,
                    attemptsUsed = attemptsUsed,
                    maxAttempts = maxAttempts,
                    score = score,
                    isWin = isWin,
                    guessesCsv = guesses.joinToString(",") { it.value.toString() }
                )
            )
        }
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        playTapFeedback()
        viewModelScope.launch {
            repository.setSelectedDifficulty(difficulty)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
            if (enabled) soundEffects.playTap()
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
            if (enabled) haptics.tap()
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        playTapFeedback()
        viewModelScope.launch {
            repository.setMusicEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        playTapFeedback()
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun setLanguage(language: AppLanguage) {
        playTapFeedback()
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }

    fun resetStatistics() {
        playTapFeedback()
        viewModelScope.launch {
            repository.resetAllStatistics()
        }
    }

    fun playTapFeedback() {
        if (settingsState.value.soundEnabled) soundEffects.playTap()
        if (settingsState.value.vibrationEnabled) haptics.tap()
    }

    fun onAppForeground() {
        musicController.onAppForeground()
    }

    fun onAppBackground() {
        musicController.onAppBackground()
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}

class GameViewModelFactory(
    private val repository: GameRepository,
    private val numberGenerator: NumberGenerator,
    private val soundEffects: SoundEffectsController,
    private val musicController: BackgroundMusicController,
    private val haptics: HapticController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(
                repository = repository,
                numberGenerator = numberGenerator,
                soundEffects = soundEffects,
                musicController = musicController,
                haptics = haptics
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

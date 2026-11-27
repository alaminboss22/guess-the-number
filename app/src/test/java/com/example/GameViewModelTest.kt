package com.example

import com.example.audio.NoOpBackgroundMusicController
import com.example.audio.NoOpHapticController
import com.example.audio.SoundEffectsController
import com.example.data.local.GameRecordEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.DefaultNumberGenerator
import com.example.data.repository.GameRepository
import com.example.data.repository.NumberGenerator
import com.example.model.AppLanguage
import com.example.model.GameDifficulty
import com.example.model.GuessFeedback
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeGameRepository : GameRepository {
    private val prefsFlow = MutableStateFlow(UserPreferences())
    private val recordsFlow = MutableStateFlow<List<GameRecordEntity>>(emptyList())

    override val userPreferences: Flow<UserPreferences> = prefsFlow
    override val gameHistory: Flow<List<GameRecordEntity>> = recordsFlow
    override val bestScore: Flow<Int> = recordsFlow.map { list -> list.maxOfOrNull { it.score } ?: 0 }
    override val totalGames: Flow<Int> = recordsFlow.map { it.size }
    override val winsCount: Flow<Int> = recordsFlow.map { list -> list.count { it.isWin } }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        prefsFlow.value = prefsFlow.value.copy(soundEnabled = enabled)
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        prefsFlow.value = prefsFlow.value.copy(vibrationEnabled = enabled)
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        prefsFlow.value = prefsFlow.value.copy(musicEnabled = enabled)
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        prefsFlow.value = prefsFlow.value.copy(darkMode = enabled)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        prefsFlow.value = prefsFlow.value.copy(language = language)
    }

    override suspend fun setSelectedDifficulty(difficulty: GameDifficulty) {
        prefsFlow.value = prefsFlow.value.copy(selectedDifficulty = difficulty)
    }

    override suspend fun saveGameRecord(record: GameRecordEntity) {
        recordsFlow.value = listOf(record.copy(id = (recordsFlow.value.size + 1).toLong())) + recordsFlow.value
    }

    override suspend fun resetAllStatistics() {
        recordsFlow.value = emptyList()
    }
}

private class RecordingSoundEffects : SoundEffectsController {
    var tapCount = 0
    var tooHighCount = 0
    var tooLowCount = 0
    var hintCount = 0
    var errorCount = 0
    var winCount = 0
    var loseCount = 0

    override fun playTap() { tapCount++ }
    override fun playTooHigh() { tooHighCount++ }
    override fun playTooLow() { tooLowCount++ }
    override fun playHint() { hintCount++ }
    override fun playError() { errorCount++ }
    override fun playWin() { winCount++ }
    override fun playLose() { loseCount++ }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeGameRepository
    private lateinit var sound: RecordingSoundEffects

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeGameRepository()
        sound = RecordingSoundEffects()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default random number generator always produces values in 1 to 100`() {
        val generator = DefaultNumberGenerator()
        repeat(500) {
            val n = generator.nextSecretNumber()
            assertTrue("Generated $n out of bounds", n in 1..100)
        }
    }

    @Test
    fun `all three difficulty modes set exact attempt limits`() = runTest {
        val vm = GameViewModel(
            repository = repository,
            numberGenerator = NumberGenerator { 42 },
            soundEffects = sound,
            musicController = NoOpBackgroundMusicController,
            haptics = NoOpHapticController
        )

        vm.startNewGame(GameDifficulty.EASY)
        assertEquals(15, vm.gameState.value.attemptsRemaining)
        assertEquals(15, vm.gameState.value.maxAttempts)

        vm.startNewGame(GameDifficulty.MEDIUM)
        assertEquals(10, vm.gameState.value.attemptsRemaining)
        assertEquals(10, vm.gameState.value.maxAttempts)

        vm.startNewGame(GameDifficulty.HARD)
        assertEquals(5, vm.gameState.value.attemptsRemaining)
        assertEquals(5, vm.gameState.value.maxAttempts)
    }

    @Test
    fun `too high, too low, hint, win and restart flow works end to end`() = runTest {
        val vm = GameViewModel(
            repository = repository,
            numberGenerator = NumberGenerator { 64 },
            soundEffects = sound,
            musicController = NoOpBackgroundMusicController,
            haptics = NoOpHapticController
        )
        advanceUntilIdle()

        vm.startNewGame(GameDifficulty.MEDIUM)

        // Guess 80 -> Too High
        vm.appendDigit(8)
        vm.appendDigit(0)
        vm.submitGuess()
        assertEquals(GuessFeedback.TOO_HIGH, vm.gameState.value.feedback)
        assertEquals(9, vm.gameState.value.attemptsRemaining)
        assertEquals(79, vm.gameState.value.maxPossible)
        assertEquals(1, sound.tooHighCount)

        // Guess 30 -> Too Low
        vm.appendDigit(3)
        vm.appendDigit(0)
        vm.submitGuess()
        assertEquals(GuessFeedback.TOO_LOW, vm.gameState.value.feedback)
        assertEquals(8, vm.gameState.value.attemptsRemaining)
        assertEquals(31, vm.gameState.value.minPossible)
        assertEquals(1, sound.tooLowCount)

        // Request hint
        vm.requestHint()
        assertEquals(1, vm.gameState.value.hintsUsed)
        assertNotNull(vm.activeHint.value)
        assertEquals(1, sound.hintCount)

        // Guess 64 -> Win
        vm.appendDigit(6)
        vm.appendDigit(4)
        vm.submitGuess()
        advanceUntilIdle()
        assertEquals(GuessFeedback.CORRECT, vm.gameState.value.feedback)
        assertTrue(vm.gameState.value.isGameOver)
        assertTrue(vm.gameState.value.lastResult!!.isWin)
        assertEquals(1, sound.winCount)
        assertEquals(1, vm.settingsState.value.totalGames)
        assertTrue(vm.settingsState.value.bestScore >= 100)

        // Restart game resets attempts, guesses, hints
        vm.restartGame()
        assertFalse(vm.gameState.value.isGameOver)
        assertEquals(10, vm.gameState.value.attemptsRemaining)
        assertTrue(vm.gameState.value.guesses.isEmpty())
        assertEquals(0, vm.gameState.value.hintsUsed)
    }

    @Test
    fun `exhausting all attempts on hard mode triggers game over loss`() = runTest {
        val vm = GameViewModel(
            repository = repository,
            numberGenerator = NumberGenerator { 99 },
            soundEffects = sound,
            musicController = NoOpBackgroundMusicController,
            haptics = NoOpHapticController
        )
        advanceUntilIdle()

        vm.startNewGame(GameDifficulty.HARD)
        for (guess in listOf(10, 20, 30, 40, 50)) {
            vm.updateDirectInput(guess.toString())
            vm.submitGuess()
        }
        advanceUntilIdle()

        assertTrue(vm.gameState.value.isGameOver)
        val result = vm.gameState.value.lastResult!!
        assertFalse(result.isWin)
        assertEquals(0, result.score)
        assertEquals(99, result.secretNumber)
        assertEquals(5, result.attemptsUsed)
        assertEquals(1, sound.loseCount)
    }

    @Test
    fun `sound setting disables sound effects and reset statistics clears scores`() = runTest {
        val vm = GameViewModel(
            repository = repository,
            numberGenerator = NumberGenerator { 50 },
            soundEffects = sound,
            musicController = NoOpBackgroundMusicController,
            haptics = NoOpHapticController
        )
        advanceUntilIdle()

        vm.setSoundEnabled(false)
        advanceUntilIdle()
        val beforeTaps = sound.tapCount
        vm.appendDigit(5)
        assertEquals(beforeTaps, sound.tapCount)

        // Play a winning game
        vm.appendDigit(0)
        vm.submitGuess()
        advanceUntilIdle()
        assertEquals(1, vm.settingsState.value.totalGames)

        // Reset statistics
        vm.resetStatistics()
        advanceUntilIdle()
        assertEquals(0, vm.settingsState.value.totalGames)
        assertEquals(0, vm.settingsState.value.bestScore)
    }
}

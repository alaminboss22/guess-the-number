package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoOpBackgroundMusicController
import com.example.audio.NoOpHapticController
import com.example.audio.NoOpSoundEffectsController
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.NumberGenerator
import com.example.data.repository.OfflineGameRepository
import com.example.localization.LocalAppStrings
import com.example.localization.stringsForLanguage
import com.example.navigation.AppNavGraph
import com.example.navigation.AppRoutes
import com.example.ui.theme.GuessTheNumberTheme
import com.example.ui.viewmodel.GameViewModel
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class GuessTheNumberUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var db: AppDatabase? = null

    @After
    fun tearDown() {
        db?.close()
    }

    private fun launchApp(secretNumber: Int = 42, startRoute: String = AppRoutes.ROUTE_HOME): GameViewModel {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db = inMemoryDb
        val repository = OfflineGameRepository(
            dao = inMemoryDb.gameRecordDao(),
            settingsDataStore = SettingsDataStore(context)
        )
        val viewModel = GameViewModel(
            repository = repository,
            numberGenerator = NumberGenerator { secretNumber },
            soundEffects = NoOpSoundEffectsController,
            musicController = NoOpBackgroundMusicController,
            haptics = NoOpHapticController
        )

        composeRule.setContent {
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppStrings provides stringsForLanguage(settings.language)) {
                GuessTheNumberTheme(darkTheme = settings.darkMode) {
                    AppNavGraph(
                        viewModel = viewModel,
                        onExitApp = {},
                        startDestination = startRoute
                    )
                }
            }
        }
        return viewModel
    }

    @Test
    fun `home screen buttons, difficulty selection, gameplay win and result navigation work`() {
        launchApp(secretNumber = 42)
        composeRule.waitForIdle()

        // Verify Home screen buttons exist
        composeRule.onNodeWithTag("start_game_button").assertIsDisplayed()
        composeRule.onNodeWithTag("difficulty_button").assertIsDisplayed()
        composeRule.onNodeWithTag("how_to_play_button").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("history_button").assertIsDisplayed()

        // Open How To Play dialog and dismiss
        composeRule.onNodeWithTag("how_to_play_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("how_to_play_got_it").performClick()
        composeRule.waitForIdle()

        // Switch difficulty to Hard (5 attempts)
        composeRule.onNodeWithTag("difficulty_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("difficulty_option_hard").performClick()
        composeRule.waitForIdle()

        // Start game
        composeRule.onNodeWithTag("start_game_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("attempts_remaining_text").assertIsDisplayed()

        // Enter 50 (Too High)
        composeRule.onNodeWithTag("keypad_5").performClick()
        composeRule.onNodeWithTag("keypad_0").performClick()
        composeRule.onNodeWithTag("submit_guess_button").performClick()
        composeRule.waitForIdle()

        // Request a hint
        composeRule.onNodeWithTag("hint_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("active_hint_text").assertIsDisplayed()

        // Enter 42 (Win!)
        composeRule.onNodeWithTag("keypad_4").performClick()
        composeRule.onNodeWithTag("keypad_2").performClick()
        composeRule.onNodeWithTag("submit_guess_button").performClick()

        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()

        // Result Screen shown with secret number & Play Again / Home buttons
        composeRule.onNodeWithTag("result_status_title").assertIsDisplayed()
        composeRule.onNodeWithTag("result_secret_number").assertIsDisplayed()
        composeRule.onNodeWithTag("play_again_button").assertIsDisplayed()
        composeRule.onNodeWithTag("result_home_button").assertIsDisplayed()
    }

    @Test
    fun `settings screen toggles and history screen render without errors`() {
        launchApp()
        composeRule.waitForIdle()

        // Navigate to Settings
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("switch_sound").performClick()
        composeRule.onNodeWithTag("switch_vibration").performClick()
        composeRule.onNodeWithTag("switch_music").performClick()
        composeRule.onNodeWithTag("switch_dark_mode").performClick()
        composeRule.onNodeWithTag("lang_chip_es").performClick()
        composeRule.waitForIdle()

        // Switch back to English and go Home
        composeRule.onNodeWithTag("lang_chip_en").performClick()
        composeRule.onNodeWithTag("settings_back_button").performClick()
        composeRule.waitForIdle()

        // Navigate to History
        composeRule.onNodeWithTag("history_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("history_empty_text").assertIsDisplayed()
    }
}

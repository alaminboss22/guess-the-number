package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AndroidHapticController
import com.example.audio.SynthesizedMusicController
import com.example.audio.SynthesizedSoundEffectsController
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.DefaultNumberGenerator
import com.example.data.repository.OfflineGameRepository
import com.example.localization.LocalAppStrings
import com.example.localization.stringsForLanguage
import com.example.navigation.AppNavGraph
import com.example.ui.theme.GuessTheNumberTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var musicController: SynthesizedMusicController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)
        val repository = OfflineGameRepository(
            dao = database.gameRecordDao(),
            settingsDataStore = settingsDataStore
        )
        val soundEffects = SynthesizedSoundEffectsController()
        musicController = SynthesizedMusicController()
        val haptics = AndroidHapticController(applicationContext)

        val factory = GameViewModelFactory(
            repository = repository,
            numberGenerator = DefaultNumberGenerator(),
            soundEffects = soundEffects,
            musicController = musicController,
            haptics = haptics
        )

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                musicController.onAppForeground()
            }

            override fun onPause(owner: LifecycleOwner) {
                musicController.onAppBackground()
            }
        })

        setContent {
            val gameViewModel: GameViewModel = viewModel(factory = factory)
            val settings by gameViewModel.settingsState.collectAsStateWithLifecycle()
            val localizedStrings = stringsForLanguage(settings.language)

            CompositionLocalProvider(LocalAppStrings provides localizedStrings) {
                GuessTheNumberTheme(darkTheme = settings.darkMode) {
                    AppNavGraph(
                        viewModel = gameViewModel,
                        onExitApp = { finish() }
                    )
                }
            }
        }
    }
}

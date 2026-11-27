package com.example.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localization.LocalAppStrings
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.GameViewModel

object AppRoutes {
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_HOME = "home"
    const val ROUTE_GAME = "game"
    const val ROUTE_RESULT = "result"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_HISTORY = "history"
}

@Composable
fun AppNavGraph(
    viewModel: GameViewModel,
    onExitApp: () -> Unit,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.ROUTE_SPLASH
) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val activeHint by viewModel.activeHint.collectAsStateWithLifecycle()
    val duplicateFlag by viewModel.duplicateGuessFlag.collectAsStateWithLifecycle()
    val history by viewModel.gameHistory.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(240)) + slideInHorizontally(animationSpec = tween(240)) { it / 8 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { -it / 10 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(240)) + slideInHorizontally(animationSpec = tween(240)) { -it / 8 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { it / 8 }
        }
    ) {
        composable(AppRoutes.ROUTE_SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(AppRoutes.ROUTE_HOME) {
                        popUpTo(AppRoutes.ROUTE_SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.ROUTE_HOME) {
            HomeScreen(
                settings = settings,
                onStartGame = { difficulty ->
                    viewModel.startNewGame(difficulty)
                    navController.navigate(AppRoutes.ROUTE_GAME) {
                        launchSingleTop = true
                    }
                },
                onSelectDifficulty = { difficulty ->
                    viewModel.selectDifficulty(difficulty)
                },
                onOpenSettings = {
                    viewModel.playTapFeedback()
                    navController.navigate(AppRoutes.ROUTE_SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    viewModel.playTapFeedback()
                    navController.navigate(AppRoutes.ROUTE_HISTORY) {
                        launchSingleTop = true
                    }
                },
                onToggleSound = {
                    viewModel.setSoundEnabled(!settings.soundEnabled)
                },
                onExitApp = onExitApp
            )
        }

        composable(AppRoutes.ROUTE_GAME) {
            GameScreen(
                gameState = gameState,
                activeHintText = viewModel.resolveHintText(activeHint, strings),
                duplicateGuessFlag = duplicateFlag,
                soundEnabled = settings.soundEnabled,
                onDigitClick = viewModel::appendDigit,
                onBackspaceClick = viewModel::backspaceInput,
                onClearClick = viewModel::clearInput,
                onSubmitGuess = viewModel::submitGuess,
                onRequestHint = viewModel::requestHint,
                onRestartGame = viewModel::restartGame,
                onToggleSound = { viewModel.setSoundEnabled(!settings.soundEnabled) },
                onNavigateBackHome = {
                    navController.popBackStack(AppRoutes.ROUTE_HOME, inclusive = false)
                },
                onGameFinished = {
                    navController.navigate(AppRoutes.ROUTE_RESULT) {
                        popUpTo(AppRoutes.ROUTE_GAME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.ROUTE_RESULT) {
            ResultScreen(
                result = gameState.lastResult,
                onPlayAgain = {
                    val difficulty = gameState.lastResult?.difficulty ?: settings.selectedDifficulty
                    viewModel.startNewGame(difficulty)
                    navController.navigate(AppRoutes.ROUTE_GAME) {
                        popUpTo(AppRoutes.ROUTE_RESULT) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoHome = {
                    viewModel.playTapFeedback()
                    navController.popBackStack(AppRoutes.ROUTE_HOME, inclusive = false)
                }
            )
        }

        composable(AppRoutes.ROUTE_SETTINGS) {
            SettingsScreen(
                settings = settings,
                onSoundChanged = viewModel::setSoundEnabled,
                onVibrationChanged = viewModel::setVibrationEnabled,
                onMusicChanged = viewModel::setMusicEnabled,
                onDarkModeChanged = viewModel::setDarkMode,
                onLanguageSelected = viewModel::setLanguage,
                onResetStatistics = viewModel::resetStatistics,
                onNavigateBack = {
                    viewModel.playTapFeedback()
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.ROUTE_HISTORY) {
            HistoryScreen(
                records = history,
                settings = settings,
                onClearHistory = viewModel::resetStatistics,
                onNavigateBack = {
                    viewModel.playTapFeedback()
                    navController.popBackStack()
                }
            )
        }
    }
}
